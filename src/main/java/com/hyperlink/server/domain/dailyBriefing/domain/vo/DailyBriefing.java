package com.hyperlink.server.domain.dailyBriefing.domain.vo;

import com.hyperlink.server.domain.dailyBriefing.dto.StatisticsByCategoryResponse;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DailyBriefing {

  private MemberStatistics memberStatistics;
  private ViewStatistics viewStatistics;
  private List<StatisticsByCategoryResponse> viewByCategories;
  private List<ContentStatistics> contentIncreaseForWeek;
  private List<StatisticsByCategoryResponse> memberCountByAttentionCategories;

  public DailyBriefing(MemberStatistics memberStatistics, ViewStatistics viewStatistics,
      List<StatisticsByCategoryResponse> viewByCategories,
      List<ContentStatistics> contentIncreaseForWeek,
      List<StatisticsByCategoryResponse> memberCountByAttentionCategories) {
    this.memberStatistics = memberStatistics;
    this.viewStatistics = viewStatistics;
    this.viewByCategories = viewByCategories;
    this.contentIncreaseForWeek = contentIncreaseForWeek;
    this.memberCountByAttentionCategories = memberCountByAttentionCategories;
  }

  public void changeContentIncreaseForWeek(List<ContentStatistics> contentIncreaseForWeek) {
    this.contentIncreaseForWeek = contentIncreaseForWeek;
  }
}
