package com.hyperlink.server.domain.creator.dto;

public record GetCreatorRecommendResponse(
    Long creatorId,
    String creatorName,
    String profileImgUrl,
    String creatorDescription,
    int subscriberAmount
) {

}
