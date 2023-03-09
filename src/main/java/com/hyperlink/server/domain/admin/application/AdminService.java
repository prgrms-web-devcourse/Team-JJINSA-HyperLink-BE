package com.hyperlink.server.domain.admin.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.domain.admin.domain.vo.CategoryAndView;
import com.hyperlink.server.domain.admin.dto.CategoryViewResponse;
import com.hyperlink.server.domain.admin.dto.CategoryViewResponses;
import com.hyperlink.server.domain.admin.dto.CountingViewByCategoryDto;
import com.hyperlink.server.domain.category.domain.CategoryRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminService {

  private final CategoryRepository categoryRepository;
  private final RedisTemplate<String, String> categoryViewRedisTemplate;
  private static final int VIEW_COUNT_AGGREGATION_START_FROM = 7;
  private static final String STANDARD_TIME_PATTERN = "yyyy-MM-dd";
  private static final String CATEGORY_VIEW = "category-view";

  public CategoryViewResponses countViewCountByCategory() {
    LocalDate now = LocalDate.now();
    List<CategoryViewResponse> weeklyViewCounts = new LinkedList<>();
    for (int nDay = VIEW_COUNT_AGGREGATION_START_FROM; nDay > 0; nDay--) {
      String targetDate = now.minusDays(nDay).toString();
      List<CountingViewByCategoryDto> viewCountsByCategories = categoryRepository.countViewsByCategoryAndDate(
          targetDate);
      CategoryViewResponse viewCountsForOneDay = CategoryViewResponse.of(
          viewCountsByCategories.stream()
              .map(viewCountsByCategory -> new CategoryAndView(
                  viewCountsByCategory.getCategoryName(), viewCountsByCategory.getViewCount()))
              .collect(Collectors.toList()), targetDate);
      weeklyViewCounts.add(viewCountsForOneDay);
    }
    return new CategoryViewResponses(weeklyViewCounts, now.toString());
  }

  public Optional<CategoryViewResponses> getCategoryView() {
    String standardTime = LocalDate.now().format(DateTimeFormatter.ofPattern(STANDARD_TIME_PATTERN));
    String categoryView = categoryViewRedisTemplate.opsForValue().get(CATEGORY_VIEW);
    if (categoryView == null) {
      return Optional.empty();
    }
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return Optional.of(objectMapper.readValue(categoryView, CategoryViewResponses.class));
    } catch (Exception e) {
      log.error(e.getMessage());
      return Optional.empty();
    }
  }
}
