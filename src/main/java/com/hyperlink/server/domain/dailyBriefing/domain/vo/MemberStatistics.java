package com.hyperlink.server.domain.dailyBriefing.domain.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberStatistics {

  private int increase;
  private long totalCount;

  public MemberStatistics(int increase, long totalCount) {
    this.increase = increase;
    this.totalCount = totalCount;
  }
}
