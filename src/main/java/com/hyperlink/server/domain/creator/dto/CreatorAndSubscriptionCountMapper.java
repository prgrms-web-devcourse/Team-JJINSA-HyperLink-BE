package com.hyperlink.server.domain.creator.dto;

public interface CreatorAndSubscriptionCountMapper {

  Long getCreatorId();
  String getName();
  Integer getSubscriberAmount();
  String getDescription();
  String getProfileImgUrl();
}
