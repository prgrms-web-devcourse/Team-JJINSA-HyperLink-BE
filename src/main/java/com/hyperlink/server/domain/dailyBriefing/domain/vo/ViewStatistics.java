package com.hyperlink.server.domain.dailyBriefing.domain.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ViewStatistics {

  private int viewIncrease;
  private long totalViewCount;

  public ViewStatistics(int viewIncrease, long totalViewCount) {
    this.viewIncrease = viewIncrease;
    this.totalViewCount = totalViewCount;
  }
}
