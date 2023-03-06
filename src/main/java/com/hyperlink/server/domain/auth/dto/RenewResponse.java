package com.hyperlink.server.domain.auth.dto;

public record RenewResponse(
    Boolean admin,
    String accessToken
) {

  public static RenewResponse from(Boolean admin, String accessToken) {
    return new RenewResponse(admin, accessToken);
  }
}
