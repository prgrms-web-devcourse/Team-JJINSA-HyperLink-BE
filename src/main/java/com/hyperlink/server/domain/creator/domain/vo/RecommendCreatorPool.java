package com.hyperlink.server.domain.creator.domain.vo;

import java.util.List;
import lombok.Getter;

@Getter
public class RecommendCreatorPool {

  Long memberId;
  List<CreatorScore> creatorScores;

  public RecommendCreatorPool(Long memberId, List<CreatorScore> creatorScores) {
    this.memberId = memberId;
    this.creatorScores = creatorScores;
  }
}
