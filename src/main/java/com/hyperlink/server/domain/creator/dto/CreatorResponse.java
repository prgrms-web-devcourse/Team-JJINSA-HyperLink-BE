package com.hyperlink.server.domain.creator.dto;

public record CreatorResponse(
    Long creatorId,
    String creatorName,
    int subscriberAmount,
    String creatorDescription,
    boolean isSubscribed,
    String profileImgUrl) {

  public static CreatorResponse of(CreatorAndSubscriptionCountMapper creator, boolean isSubscribed) {
    return new CreatorResponse(creator.getCreatorId(), creator.getName(), creator.getSubscriberAmount(),
        creator.getDescription(), isSubscribed, creator.getProfileImgUrl());
  }
}
