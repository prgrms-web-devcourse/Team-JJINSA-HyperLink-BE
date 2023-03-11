package com.hyperlink.server.domain.dailyBriefing.domain.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberStatistics {

  private int memberIncrease;
  private long totalMemberCount;

  public MemberStatistics(int memberIncrease, long totalMemberCount) {
    this.memberIncrease = memberIncrease;
    this.totalMemberCount = totalMemberCount;
  }
}
