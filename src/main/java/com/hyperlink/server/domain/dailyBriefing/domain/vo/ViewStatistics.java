package com.hyperlink.server.domain.dailyBriefing.domain.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ViewStatistics {

  private int increase;
  private long totalCount;

  public ViewStatistics(int increase, long totalCount) {
    this.increase = increase;
    this.totalCount = totalCount;
  }
}
