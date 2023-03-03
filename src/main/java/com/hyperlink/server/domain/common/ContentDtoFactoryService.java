package com.hyperlink.server.domain.common;

import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.dto.ContentResponse;
import com.hyperlink.server.domain.content.dto.GetContentsCommonResponse;
import com.hyperlink.server.domain.content.dto.RecommendationCompanyResponse;
import com.hyperlink.server.domain.memberContent.application.MemberContentService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentDtoFactoryService {

  private final MemberContentService memberContentService;

  public GetContentsCommonResponse createContentResponses(Long memberId, List<Content> contents,
      boolean hasNext) {
    List<ContentResponse> contentResponses = new ArrayList<>();
    for (Content content : contents) {
      boolean isBookmarked = memberContentService.isBookmarked(memberId, content.getId());
      boolean isLiked = content.getLikeCount() > 0;
      // TODO : 회사 추천 리스트 추가
      List<RecommendationCompanyResponse> recommendationCompanyResponses = new ArrayList<>();

      ContentResponse contentResponse = new ContentResponse(content.getId(), content.getTitle(),
          content.getCreator().getName(), content.getCreator().getId(), content.getContentImgUrl(),
          content.getLink(), content.getLikeCount(), content.getViewCount(), isBookmarked, isLiked,
          content.getCreatedAt().toString(), recommendationCompanyResponses);
      contentResponses.add(contentResponse);
    }

    return new GetContentsCommonResponse(contentResponses, hasNext);
  }

}
