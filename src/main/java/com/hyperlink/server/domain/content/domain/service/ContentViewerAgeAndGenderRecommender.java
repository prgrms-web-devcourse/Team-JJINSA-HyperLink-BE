package com.hyperlink.server.domain.content.domain.service;

import com.hyperlink.server.domain.content.dto.ContentViewerRecommendationResponse;
import com.hyperlink.server.domain.memberContent.domain.MemberContentRepository;
import com.hyperlink.server.domain.memberContent.dto.ContentViewerAgeAndGenderRecommendDto;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentViewerAgeAndGenderRecommender implements ContentViewerRecommender{

  private static final int STANDARD_RECOMMEND_COUNT = 3;
  private final MemberContentRepository memberContentRepository;

  @Override
  public List<ContentViewerRecommendationResponse> getContentViewerRecommendationResponse(Long contentId) {
    LocalDate now = LocalDate.now();
    List<ContentViewerAgeAndGenderRecommendDto> contentViewerAgeAndGenderRecommendDtos = memberContentRepository.recommendAgeAndGender(
        now, contentId, STANDARD_RECOMMEND_COUNT);
    return contentViewerAgeAndGenderRecommendDtos.stream().map(
        ContentViewerRecommendationResponse::from).collect(Collectors.toList());
  }
}
