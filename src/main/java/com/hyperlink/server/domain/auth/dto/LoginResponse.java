package com.hyperlink.server.domain.auth.dto;

public record LoginResponse(
    String accessToken
) {

  public static LoginResponse from(String accessToken) {
    return new LoginResponse(accessToken);
  }
}
