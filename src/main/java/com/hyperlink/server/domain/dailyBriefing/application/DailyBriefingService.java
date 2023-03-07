package com.hyperlink.server.domain.dailyBriefing.application;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.domain.vo.CategoryType;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.dailyBriefing.domain.vo.CountStatisticsByCategory;
import com.hyperlink.server.domain.dailyBriefing.dto.DailyBriefing;
import com.hyperlink.server.domain.dailyBriefing.dto.GetDailyBriefingResponse;
import com.hyperlink.server.domain.dailyBriefing.dto.StatisticsByCategoryResponse;
import com.hyperlink.server.domain.dailyBriefing.infrastructure.DailyBriefingRepositoryCustom;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.memberHistory.domain.MemberHistoryRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DailyBriefingService {

  public static final int STANDARD_HOUR = 24;

  private final MemberRepository memberRepository;
  private final MemberHistoryRepository memberHistoryRepository;
  private final ContentRepository contentRepository;
  private final CategoryRepository categoryRepository;
  private final DailyBriefingRepositoryCustom dailyBriefingRepositoryCustom;

  public GetDailyBriefingResponse getDailyBriefing(LocalDateTime standardTime) {
    LocalDateTime pastOneDay = standardTime.minusHours(STANDARD_HOUR);

    Integer memberIncrease = memberRepository.countByCreatedAtAfter(pastOneDay);
    List<String> findCategoryNameOfHistoryPastOneDayAfter = memberHistoryRepository.findAllByCreatedAtAfter(
        pastOneDay);
    int viewIncrease = findCategoryNameOfHistoryPastOneDayAfter.size();
    List<StatisticsByCategoryResponse> viewByCategories = getViewAndRankingByCategories(
        findCategoryNameOfHistoryPastOneDayAfter);

    LocalDateTime startOfToday = standardTime.toLocalDate().atStartOfDay();
    Integer contentIncrease = contentRepository.countByCreatedAtAfter(startOfToday);
    List<StatisticsByCategoryResponse> memberCountByAttentionCategories = getMemberCountAndRankingByAttentionCategories();

    DailyBriefing dailyBriefing = new DailyBriefing(memberIncrease, viewIncrease, viewByCategories,
        contentIncrease, memberCountByAttentionCategories);
    return new GetDailyBriefingResponse(
        standardTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH")), dailyBriefing);
  }

  public List<StatisticsByCategoryResponse> getViewAndRankingByCategories(
      List<String> categoryNameOfHistoryPastOneDayAfter) {
    Map<String, CountStatisticsByCategory> viewCountStatisticsByCategoryHashMap = new HashMap<>();
    for (CategoryType categoryType : CategoryType.values()) {
      CountStatisticsByCategory countStatisticsByCategory = new CountStatisticsByCategory(
          categoryType);
      viewCountStatisticsByCategoryHashMap.put(categoryType.getRequestParamName(),
          countStatisticsByCategory);
    }

    for (String categoryName : categoryNameOfHistoryPastOneDayAfter) {
      viewCountStatisticsByCategoryHashMap.get(categoryName).addViewCountStatistics();
    }

    return includeCalculateRankingAndCreateStatisticsByCategoryResponse(viewCountStatisticsByCategoryHashMap);
  }

  public List<StatisticsByCategoryResponse> getMemberCountAndRankingByAttentionCategories() {
    Map<String, CountStatisticsByCategory> memberCountStatisticsByCategoryHashMap = new HashMap<>();

    List<Category> allCategory = categoryRepository.findAll();
    for (Category category : allCategory) {
      long memberCount = dailyBriefingRepositoryCustom.getMemberCountByAttentionCategories(
          category.getId()).orElseGet(() -> 0L);
      CountStatisticsByCategory countStatisticsByCategory = new CountStatisticsByCategory(
          CategoryType.of(category.getName()), memberCount);
      memberCountStatisticsByCategoryHashMap.put(category.getName(), countStatisticsByCategory);
    }

    return includeCalculateRankingAndCreateStatisticsByCategoryResponse(memberCountStatisticsByCategoryHashMap);
  }

  public List<StatisticsByCategoryResponse> includeCalculateRankingAndCreateStatisticsByCategoryResponse(Map<String, CountStatisticsByCategory> countStatisticsByCategoryHashMap) {
    List<CountStatisticsByCategory> countStatisticsByCategoriesForRanking = new ArrayList<>(
        countStatisticsByCategoryHashMap.values());
    calculateRanking(countStatisticsByCategoriesForRanking);

    List<StatisticsByCategoryResponse> statisticsByCategories = new ArrayList<>();
    countStatisticsByCategoryHashMap.forEach((categoryName, countStatisticsByCategory) -> {
      StatisticsByCategoryResponse statisticsByCategoryResponse = new StatisticsByCategoryResponse(
          categoryName, countStatisticsByCategory.getCount(),
          countStatisticsByCategory.getRanking());
      statisticsByCategories.add(statisticsByCategoryResponse);
    });
    return statisticsByCategories;
  }

  public void calculateRanking(
      List<CountStatisticsByCategory> countStatisticsByCategorylist) {
    int categorySize = countStatisticsByCategorylist.size();

    for (int i = 0; i < categorySize; i++) {
      for (int j = 0; j < categorySize; j++) {
        if (countStatisticsByCategorylist.get(i).getCount() < countStatisticsByCategorylist.get(j)
            .getCount()) {
          countStatisticsByCategorylist.get(i).upRanking();
        }
      }
    }
  }
}
