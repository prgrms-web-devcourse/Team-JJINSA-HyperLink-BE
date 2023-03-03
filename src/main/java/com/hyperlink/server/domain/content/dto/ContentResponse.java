package com.hyperlink.server.domain.content.dto;

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
    List<RecommendationCompanyResponse> recommendationCompanies
) {

}
