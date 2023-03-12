package com.hyperlink.server.dailyBriefing.domain.vo;

import static com.hyperlink.server.domain.category.domain.vo.CategoryType.BEAUTY;
import static com.hyperlink.server.domain.category.domain.vo.CategoryType.DEVELOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hyperlink.server.domain.dailyBriefing.domain.vo.CountStatisticsByCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CountStatisticsByCategory VO 테스트")
public class CountStatisticsByCategoryTest {

  @Nested
  class EqualsAndHashCodeTest {

    @Test
    @DisplayName("CountStatisticsByCategory는 categoryType, statisticsType이 같을 경우 같은 객체이다")
    void equalsAndHashCodeTest() {
      CountStatisticsByCategory developStatisticsOne = new CountStatisticsByCategory(DEVELOP);
      CountStatisticsByCategory developStatisticsTwo = new CountStatisticsByCategory(DEVELOP);

      boolean equals = developStatisticsOne.equals(developStatisticsTwo);

      assertTrue(equals);
      assertThat(developStatisticsOne).hasSameHashCodeAs(developStatisticsTwo);
    }

    @Test
    @DisplayName("CountStatisticsByCategory는 주소값이 같을 경우 같은 객체이다")
    void sameObjectEqualsAndHashCodeTest() {
      CountStatisticsByCategory developStatistics = new CountStatisticsByCategory(DEVELOP);

      boolean equals = developStatistics.equals(developStatistics);

      assertTrue(equals);
      assertThat(developStatistics).hasSameHashCodeAs(developStatistics);
    }

    @Test
    @DisplayName("CountStatisticsByCategory는 다른 객체와 equals가 false이다")
    void differentObjectTypeTest() {
      CountStatisticsByCategory developStatistics = new CountStatisticsByCategory(DEVELOP);
      String developString = "develop";

      boolean equals = developStatistics.equals(developString);

      assertFalse(equals);
    }

    @Test
    @DisplayName("CountStatisticsByCategory는 categoryType이 다를 경우 다른 객체이다")
    void differentCategoryTypeTest() {
      CountStatisticsByCategory developStatistics = new CountStatisticsByCategory(DEVELOP);
      CountStatisticsByCategory beautyStatistics = new CountStatisticsByCategory(BEAUTY);

      boolean equals = developStatistics.equals(beautyStatistics);

      assertFalse(equals);
      assertThat(developStatistics).doesNotHaveSameHashCodeAs(beautyStatistics);
    }

    @Test
    @DisplayName("CountStatisticsByCategory는 statisticsType이 다를 경우 다른 객체이다")
    void differentStatisticsTypeTest() {
      CountStatisticsByCategory developStatisticsOne = new CountStatisticsByCategory(DEVELOP);
      CountStatisticsByCategory developStatisticsTwo = new CountStatisticsByCategory(DEVELOP, 3L);

      boolean equals = developStatisticsOne.equals(developStatisticsTwo);

      assertFalse(equals);
      assertThat(developStatisticsOne).doesNotHaveSameHashCodeAs(developStatisticsTwo);
    }
  }
}
