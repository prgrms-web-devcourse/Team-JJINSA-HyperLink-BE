package com.hyperlink.server.domain.content.dto;

import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import java.util.List;

public record ContentResponse(
    Long contentId,
    String title,
    String creatorName,
    Long creatorId,
    String contentImgUrl,
    String link,
    int likeCount,
    int viewCount,
    boolean isBookmarked,
    boolean isLiked,
    String createdAt,
    List<ContentViewerRecommendationResponse> recommendations
) {

  public static ContentResponse from(Content content, boolean isBookmarked, boolean isLiked,
      List<RecommendationCompanyResponse> recommendationCompanyResponses) {
    Creator creator = content.getCreator();

    return new ContentResponse(content.getId(), content.getTitle(), creator.getName(),
        creator.getId(), content.getContentImgUrl(), content.getLink(), content.getLikeCount(),
        content.getViewCount(), isBookmarked, isLiked, content.getCreatedAt().toString(),
        recommendationCompanyResponses);
  }
}
