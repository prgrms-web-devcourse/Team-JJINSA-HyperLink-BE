package com.hyperlink.server.domain.auth.dto;

public record LoginResponse(
    Boolean admin,
    String accessToken
) {

  public static LoginResponse from(Boolean admin, String accessToken) {
    return new LoginResponse(admin, accessToken);
  }
}
