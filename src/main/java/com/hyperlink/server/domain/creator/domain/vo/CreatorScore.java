package com.hyperlink.server.domain.creator.domain.vo;

import lombok.Getter;

@Getter
public class CreatorScore {

  private final Long creatorId;
  private double score;

  public CreatorScore(Long creatorId, double score) {
    this.creatorId = creatorId;
    this.score = score;
  }

  public void addScore(double score) {
    this.score += score;
  }

  @Override
  public String toString() {
    return "CreatorScore{" +
        "creatorId=" + creatorId +
        ", score=" + score +
        '}';
  }
}
