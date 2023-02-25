package com.hyperlink.server.domain.auth.dto;

public record OauthResponse(
    String accessToken,
    boolean joinCheck,
    String email,
    String profileUrl
) {

  public static OauthResponse from(String accessToken, boolean joinCheck, String email,
      String profileUrl) {
    return new OauthResponse(accessToken, joinCheck, email, email);
  }
}
