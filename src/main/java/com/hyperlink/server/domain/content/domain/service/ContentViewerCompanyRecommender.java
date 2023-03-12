package com.hyperlink.server.domain.content.domain.service;

import com.hyperlink.server.domain.content.dto.ContentViewerRecommendationResponse;
import com.hyperlink.server.domain.memberContent.domain.MemberContentRepository;
import com.hyperlink.server.domain.memberContent.dto.ContentViewerCompanyRecommendDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentViewerCompanyRecommender implements ContentViewerRecommender{

  private final MemberContentRepository memberContentRepository;
  private static final int STANDARD_COMPANY_RECOMMEND_COUNT = 3;

  @Override
  public List<ContentViewerRecommendationResponse> getContentViewerRecommendationResponse(Long contentId) {
    List<ContentViewerCompanyRecommendDto> contentViewerCompanyRecommendDtos = memberContentRepository.recommendCompanies(
        contentId, STANDARD_COMPANY_RECOMMEND_COUNT);
    return contentViewerCompanyRecommendDtos.stream().map(ContentViewerRecommendationResponse::from)
        .toList();
  }
}
