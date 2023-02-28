package com.hyperlink.server.domain.auth.oauth.dto;

public record OauthResponse(
    String accessToken,
    boolean joinCheck,
    String email
) {

  public static OauthResponse from(String accessToken, boolean joinCheck, String email) {
    return new OauthResponse(accessToken, joinCheck, email);
  }
}
