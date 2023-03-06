package com.hyperlink.server.domain.auth.dto;

public record RenewResponse(
    String accessToken
) {

  public static RenewResponse from(String accessToken) {
    return new RenewResponse(accessToken);
  }
}
