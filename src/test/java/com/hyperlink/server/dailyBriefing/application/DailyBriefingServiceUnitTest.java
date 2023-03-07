package com.hyperlink.server.dailyBriefing.application;

import static com.hyperlink.server.domain.category.domain.vo.CategoryType.BEAUTY;
import static com.hyperlink.server.domain.category.domain.vo.CategoryType.DEVELOP;
import static com.hyperlink.server.domain.category.domain.vo.CategoryType.FINANCE;
import static com.hyperlink.server.domain.category.domain.vo.CategoryType.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.dailyBriefing.application.DailyBriefingService;
import com.hyperlink.server.domain.dailyBriefing.domain.vo.CountStatisticsByCategory;
import com.hyperlink.server.domain.dailyBriefing.dto.DailyBriefing;
import com.hyperlink.server.domain.dailyBriefing.dto.GetDailyBriefingResponse;
import com.hyperlink.server.domain.dailyBriefing.dto.StatisticsByCategoryResponse;
import com.hyperlink.server.domain.dailyBriefing.infrastructure.DailyBriefingRepositoryCustom;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.memberHistory.domain.MemberHistoryRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DailyBriefingServiceUnitTest {

  @Mock
  MemberRepository memberRepository;
  @Mock
  MemberHistoryRepository memberHistoryRepository;
  @Mock
  ContentRepository contentRepository;
  @Mock
  CategoryRepository categoryRepository;
  @Mock
  DailyBriefingRepositoryCustom dailyBriefingRepositoryCustom;
  @Spy
  @InjectMocks
  DailyBriefingService dailyBriefingService;

  @Nested
  @DisplayName("데일리브리핑 조회 메서드는")
  class GetDailyBriefingUnit {

    GetDailyBriefingResponse getDailyBriefingResponse;

    @BeforeEach
    void responseSetUp() {
      LocalDateTime standardTime = LocalDateTime.now();
      List<StatisticsByCategoryResponse> viewByCategories = List.of(
          new StatisticsByCategoryResponse("develop", 283, 3),
          new StatisticsByCategoryResponse("beauty", 832, 1),
          new StatisticsByCategoryResponse("finance", 425, 2));
      List<StatisticsByCategoryResponse> memberCountByAttentionCategories = List.of(
          new StatisticsByCategoryResponse("develop", 13, 3),
          new StatisticsByCategoryResponse("beauty", 92, 1),
          new StatisticsByCategoryResponse("finance", 55, 2));

      DailyBriefing dailyBriefing = new DailyBriefing(300, 1540, viewByCategories,
          45, memberCountByAttentionCategories);
      getDailyBriefingResponse = new GetDailyBriefingResponse(
          standardTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), dailyBriefing);
    }

    @Test
    @DisplayName("총 5가지의 통계 자료를 응답한다")
    void fourStatistics() {
      doReturn(300).when(memberRepository).countByCreatedAtAfter(any());
      doReturn(List.of("categoryName")).when(memberHistoryRepository)
          .findAllByCreatedAtAfter(any());
      doReturn(List.of(new StatisticsByCategoryResponse("develop", 283, 3))).when(
          dailyBriefingService).getViewAndRankingByCategories(any());
      doReturn(45).when(contentRepository).countByCreatedAtAfter(any());
      doReturn(List.of(new StatisticsByCategoryResponse("develop", 13, 3))).when(
          dailyBriefingService).getMemberCountAndRankingByAttentionCategories();

      dailyBriefingService.getDailyBriefing(LocalDateTime.now());

      verify(memberRepository, times(1)).countByCreatedAtAfter(any());
      verify(memberHistoryRepository, times(1)).findAllByCreatedAtAfter(any());
      verify(dailyBriefingService, times(1)).getViewAndRankingByCategories(any());
      verify(contentRepository, times(1)).countByCreatedAtAfter(any());
      verify(dailyBriefingService, times(1)).getMemberCountAndRankingByAttentionCategories();
    }
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  @DisplayName("getViewByCategories 메소드는")
  class GetViewByCategoriesTest {

    Stream<Arguments> createViewCountAndRanking() {
      return Stream.of(
          Arguments.of(DEVELOP.getRequestParamName(), 3, 1),
          Arguments.of(BEAUTY.getRequestParamName(), 1, 2),
          Arguments.of(FINANCE.getRequestParamName(), 1, 2)
      );
    }

    @ParameterizedTest
    @MethodSource("createViewCountAndRanking")
    @DisplayName("인자로 받은 history 내역에 대해서 카테고리별 조회수, 랭킹을 계산한다")
    void getViewByCategoriesTest(String categoryName, int view, int ranking) {
      List<String> categoryNameOfHistoryPastOneDayAfter = List.of(DEVELOP.getRequestParamName(),
          DEVELOP.getRequestParamName(), BEAUTY.getRequestParamName(),
          DEVELOP.getRequestParamName(),
          FINANCE.getRequestParamName());

      List<StatisticsByCategoryResponse> viewByCategories = dailyBriefingService.getViewAndRankingByCategories(
          categoryNameOfHistoryPastOneDayAfter);

      StatisticsByCategoryResponse statisticsByCategoryResponse = viewByCategories.stream()
          .filter(statistics -> statistics.categoryName()
              .equals(categoryName))
          .findFirst().orElseThrow();
      assertThat(statisticsByCategoryResponse.count()).isEqualTo(view);
      assertThat(statisticsByCategoryResponse.ranking()).isEqualTo(ranking);
    }

    @Test
    @DisplayName("24시간 동안 집계된 조회내역이 없다면 모든 카테고리에 대해서 count=0, ranking=1이 반환된다")
    void nothingHistory() {
      List<String> categoryNameOfHistoryPastOneDayAfter = List.of();

      List<StatisticsByCategoryResponse> viewByCategories = dailyBriefingService.getViewAndRankingByCategories(
          categoryNameOfHistoryPastOneDayAfter);

      for (StatisticsByCategoryResponse statisticsByCategoryResponse : viewByCategories) {
        assertThat(statisticsByCategoryResponse.count()).isZero();
        assertThat(statisticsByCategoryResponse.ranking()).isOne();
      }
    }
  }

  @Nested
  @DisplayName("getMemberCountByAttentionCategories 메소드는")
  class GetMemberCountByAttentionCategoriesTest {

    @Test
    @DisplayName("관심카테고리별 회원 숫자와 랭킹을 계산한다")
    void sameMemberCountAndRankingTest() {
      Optional<Long> optMemberCount = Optional.of(3L);
      final int ranking = 1;

      List<Category> allCategory = List.of(
          new Category(DEVELOP.getRequestParamName()),
          new Category(BEAUTY.getRequestParamName()),
          new Category(FINANCE.getRequestParamName())
      );

      doReturn(allCategory).when(categoryRepository).findAll();
      doReturn(optMemberCount).when(dailyBriefingRepositoryCustom)
          .getMemberCountByAttentionCategories(any());

      List<StatisticsByCategoryResponse> memberCountByAttentionCategories = dailyBriefingService.getMemberCountAndRankingByAttentionCategories();
      for (StatisticsByCategoryResponse statisticsByCategoryResponse : memberCountByAttentionCategories) {
        assertThat(statisticsByCategoryResponse.count()).isEqualTo(optMemberCount.get());
        assertThat(statisticsByCategoryResponse.ranking()).isEqualTo(ranking);
      }
    }

    @Test
    @DisplayName("관심카테고리별 회원 숫자가 0일 경우 count=0을 응답한다")
    void memberCountIsZero() {
      Optional<Long> optMemberCount = Optional.empty();
      final int ranking = 1;

      List<Category> allCategory = List.of(
          new Category(DEVELOP.getRequestParamName()),
          new Category(BEAUTY.getRequestParamName()),
          new Category(FINANCE.getRequestParamName())
      );

      doReturn(allCategory).when(categoryRepository).findAll();
      doReturn(optMemberCount).when(dailyBriefingRepositoryCustom)
          .getMemberCountByAttentionCategories(any());

      List<StatisticsByCategoryResponse> memberCountByAttentionCategories = dailyBriefingService.getMemberCountAndRankingByAttentionCategories();
      for (StatisticsByCategoryResponse statisticsByCategoryResponse : memberCountByAttentionCategories) {
        assertThat(statisticsByCategoryResponse.count()).isZero();
        assertThat(statisticsByCategoryResponse.ranking()).isEqualTo(ranking);
      }
    }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @DisplayName("랭킹 계산 메서드는")
  class CalculateRanking {

    List<CountStatisticsByCategory> countStatisticsByCategorylist;

    @BeforeEach
    void calculateRankingTest() {
      countStatisticsByCategorylist = List.of(
          new CountStatisticsByCategory(of("develop"), 13),
          new CountStatisticsByCategory(of("beauty"), 92),
          new CountStatisticsByCategory(of("finance"), 55)
      );

      dailyBriefingService.calculateRanking(countStatisticsByCategorylist);
    }

    Stream<Arguments> createResultRanking() {
      return Stream.of(
          Arguments.of(0, 3),
          Arguments.of(1, 1),
          Arguments.of(2, 2)
      );
    }

    @ParameterizedTest
    @MethodSource("createResultRanking")
    @DisplayName("CountStatisticsByCategory의 count 숫자가 높은 순서대로 랭킹을 계산하여 객체 내부에 저장한다")
    void verifyRankingResultTest(int index, int ranking) {
      assertThat(countStatisticsByCategorylist.get(index).getRanking()).isEqualTo(ranking);
    }

  }

}
