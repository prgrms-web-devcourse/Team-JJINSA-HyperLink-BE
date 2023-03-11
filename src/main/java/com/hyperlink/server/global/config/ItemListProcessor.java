package com.hyperlink.server.global.config;

import com.hyperlink.server.domain.creator.domain.CreatorRecommendService;
import com.hyperlink.server.domain.creator.domain.vo.CreatorScore;
import com.hyperlink.server.domain.creator.domain.vo.RecommendCreatorPool;
import com.hyperlink.server.domain.member.domain.entity.Member;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class ItemListProcessor implements ItemProcessor<Member, RecommendCreatorPool> {

  private final CreatorRecommendService creatorRecommendService;

  public ItemListProcessor(CreatorRecommendService creatorRecommendService) {
    this.creatorRecommendService = creatorRecommendService;
  }

  @Override
  public RecommendCreatorPool process(Member item) throws Exception {
    log.info("process start, memberId : {}", item.getId());

    creatorRecommendService.initCreatorScore();
    creatorRecommendService.calculateScore(item);
    List<CreatorScore> creatorScoreList = creatorRecommendService.getCreatorScoreList(item);
    return new RecommendCreatorPool(item.getId(), creatorScoreList);
  }
}
