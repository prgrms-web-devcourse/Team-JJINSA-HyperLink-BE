package com.hyperlink.server.domain.subscription.dto;

public record SubscribeResponse(Boolean isSubscribed) {

  public static SubscribeResponse of(boolean isSubscribed) {
    return new SubscribeResponse(isSubscribed);
  }
}
