package com.hyperlink.server.domain.dailyBriefing.application;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.domain.vo.CategoryType;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.dailyBriefing.domain.ContentStatisticsRepository;
import com.hyperlink.server.domain.dailyBriefing.domain.vo.ContentStatistics;
import com.hyperlink.server.domain.dailyBriefing.domain.vo.CountStatisticsByCategory;
import com.hyperlink.server.domain.dailyBriefing.domain.vo.DailyBriefing;
import com.hyperlink.server.domain.dailyBriefing.domain.vo.MemberStatistics;
import com.hyperlink.server.domain.dailyBriefing.domain.vo.ViewStatistics;
import com.hyperlink.server.domain.dailyBriefing.dto.GetDailyBriefingResponse;
import com.hyperlink.server.domain.dailyBriefing.dto.StatisticsByCategoryResponse;
import com.hyperlink.server.domain.dailyBriefing.infrastructure.DailyBriefingRepositoryCustom;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.memberHistory.domain.MemberHistoryRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyBriefingService {

  public static final int STANDARD_HOUR = 24;
  public static final String STANDARD_TIME_PATTERN = "yyyy-MM-dd HH";
  public static final String STANDARD_DATE_PATTERN = "yyyy-MM-dd";

  private final MemberRepository memberRepository;
  private final MemberHistoryRepository memberHistoryRepository;
  private final ContentRepository contentRepository;
  private final CategoryRepository categoryRepository;
  private final ContentStatisticsRepository contentStatisticsRepository;
  private final DailyBriefingRepositoryCustom dailyBriefingRepositoryCustom;
  private final RedisTemplate<String, Object> redisTemplate;

  public GetDailyBriefingResponse getDailyBriefingResponse(LocalDateTime now) {
    String todayDate = now.toLocalDate().toString();
    final long whenResponseIsNullRetryPastStandardTime = 1L;
    String standardTime = now.format(DateTimeFormatter.ofPattern(STANDARD_TIME_PATTERN));
    String redisHashKey = "daily-briefing" + todayDate;
    GetDailyBriefingResponse getDailyBriefingResponse = (GetDailyBriefingResponse) redisTemplate.opsForHash()
        .get(redisHashKey, standardTime);

    if (getDailyBriefingResponse == null) {
      getDailyBriefingResponse = getPastDailyBriefingResponse(now,
          whenResponseIsNullRetryPastStandardTime, redisHashKey);
    }

    List<ContentStatistics> contentStatisticsList = getContentStatisticsListForPastSixDays();
    ContentStatistics todayContentStatistics = getDailyBriefingResponse.dailyBriefing()
        .getContentIncreaseForWeek().get(0);
    contentStatisticsList.add(todayContentStatistics);
    getDailyBriefingResponse.dailyBriefing().changeContentIncreaseForWeek(contentStatisticsList);

    return getDailyBriefingResponse;
  }

  public GetDailyBriefingResponse getPastDailyBriefingResponse(LocalDateTime now,
      long pastStandardTime, String redisHashKey) {
    String pastStandardTimePattern = now.minusHours(pastStandardTime)
        .format(DateTimeFormatter.ofPattern(STANDARD_TIME_PATTERN));
    return (GetDailyBriefingResponse) redisTemplate.opsForHash()
        .get(redisHashKey, pastStandardTimePattern);
  }

  public List<ContentStatistics> getContentStatisticsListForPastSixDays() {
    List<ContentStatistics> contentStatisticsList = new ArrayList<>();
    for (int i = 6; i > 0; i--) {
      LocalDateTime standardDate = LocalDateTime.now().minusDays(i);
      String standardDatePattern = standardDate.format(DateTimeFormatter.ofPattern(
          STANDARD_DATE_PATTERN));

      contentStatisticsRepository.findById(
          standardDatePattern).ifPresentOrElse(contentStatisticsList::add, () -> {
        log.info("{} 컨텐츠 증가분 데이터 redis에서 찾지 못함", standardDatePattern);
        int contentCountByDate = findContentCountByDate(standardDate.toLocalDate());
        ContentStatistics contentStatistics = new ContentStatistics(standardDatePattern,
            contentCountByDate);
        contentStatisticsList.add(contentStatistics);
      });
    }
    return contentStatisticsList;
  }

  public GetDailyBriefingResponse createDailyBriefing(LocalDateTime standardTime) {
    LocalDateTime pastOneDay = standardTime.minusHours(STANDARD_HOUR);

    MemberStatistics memberStatistics = createMemberStatistics(pastOneDay);
    List<String> findCategoryNameOfHistoryPastOneDayAfter = memberHistoryRepository.findAllByCreatedAtAfter(
        pastOneDay);
    ViewStatistics viewStatistics = createViewStatistics(findCategoryNameOfHistoryPastOneDayAfter);
    List<StatisticsByCategoryResponse> viewByCategories = getViewAndRankingByCategories(
        findCategoryNameOfHistoryPastOneDayAfter);
    List<ContentStatistics> todayContentStatisticsList = createTodayContentStatisticsList(
        standardTime);
    List<StatisticsByCategoryResponse> memberCountByAttentionCategories = getMemberCountAndRankingByAttentionCategories();

    DailyBriefing dailyBriefing = new DailyBriefing(memberStatistics, viewStatistics,
        viewByCategories, todayContentStatisticsList, memberCountByAttentionCategories);
    return new GetDailyBriefingResponse(
        standardTime.format(DateTimeFormatter.ofPattern(STANDARD_TIME_PATTERN)), dailyBriefing);
  }

  private MemberStatistics createMemberStatistics(LocalDateTime standardTime) {
    long totalMemberCount = memberRepository.count();
    Integer memberIncrease = memberRepository.countByCreatedAtAfter(standardTime);
    return new MemberStatistics(memberIncrease, totalMemberCount);
  }

  private ViewStatistics createViewStatistics(
      List<String> findCategoryNameOfHistoryPastOneDayAfter) {
    long totalViewCount = memberHistoryRepository.count();
    int viewIncrease = findCategoryNameOfHistoryPastOneDayAfter.size();
    return new ViewStatistics(viewIncrease, totalViewCount);
  }

  private List<ContentStatistics> createTodayContentStatisticsList(LocalDateTime standardTime) {
    LocalDateTime startOfToday = standardTime.toLocalDate().atStartOfDay();
    Integer contentIncrease = contentRepository.countByCreatedAtAfter(startOfToday);
    String todayDate = startOfToday.format(DateTimeFormatter.ofPattern(
        STANDARD_DATE_PATTERN));
    return List.of(new ContentStatistics(todayDate, contentIncrease));
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

    return includeCalculateRankingAndCreateStatisticsByCategoryResponse(
        viewCountStatisticsByCategoryHashMap);
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

    return includeCalculateRankingAndCreateStatisticsByCategoryResponse(
        memberCountStatisticsByCategoryHashMap);
  }

  public List<StatisticsByCategoryResponse> includeCalculateRankingAndCreateStatisticsByCategoryResponse(
      Map<String, CountStatisticsByCategory> countStatisticsByCategoryHashMap) {
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

  public ContentStatistics createYesterdayContentStatistics() {
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    LocalDate yesterdayDate = yesterday.toLocalDate();
    String standardDate = yesterday.format(DateTimeFormatter.ofPattern(
        STANDARD_DATE_PATTERN));
    int contentIncrease = findContentCountByDate(yesterdayDate);
    return new ContentStatistics(standardDate, contentIncrease);
  }

  private int findContentCountByDate(LocalDate date) {
    return contentRepository.countByDate(date.toString());
  }
}
