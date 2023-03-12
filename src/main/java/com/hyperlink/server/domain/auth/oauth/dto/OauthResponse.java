package com.hyperlink.server.domain.auth.oauth.dto;

public record OauthResponse(
    String accessToken,
    boolean wasSignedUp,
    String email
) {

  public static OauthResponse from(String accessToken, boolean wasSignedUp, String email) {
    return new OauthResponse(accessToken, wasSignedUp, email);
  }
}
