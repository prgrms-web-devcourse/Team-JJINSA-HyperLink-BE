package com.hyperlink.server.domain.dailyBriefing.domain.vo;

import com.hyperlink.server.domain.category.domain.vo.CategoryType;
import java.util.Objects;
import lombok.Getter;

@Getter
public class CountStatisticsByCategory {

  private static final int RANK_INIT = 1;

  private CategoryType categoryType;
  private long count;
  private int ranking;
  private StatisticsType statisticsType;

  public CountStatisticsByCategory(CategoryType categoryType) {
    this.categoryType = categoryType;
    this.count = 0;
    this.ranking = RANK_INIT;
    this.statisticsType = StatisticsType.VIEW_COUNT;
  }

  public CountStatisticsByCategory(CategoryType categoryType, long count) {
    this.categoryType = categoryType;
    this.count = count;
    this.ranking = RANK_INIT;
    this.statisticsType = StatisticsType.MEMBER_COUNT;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CountStatisticsByCategory that)) {
      return false;
    }
    return getCategoryType() == that.getCategoryType()
        && getStatisticsType() == that.getStatisticsType();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getCategoryType(), getStatisticsType());
  }

  public void addViewCountStatistics() {
    this.count++;
  }

  public void upRanking() {
    this.ranking++;
  }
}
