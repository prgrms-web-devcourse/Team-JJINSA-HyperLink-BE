package com.hyperlink.server.domain.member.dto;


public record SignUpResponse(
    String accessToken
) {

  public static SignUpResponse from(String accessToken) {
    return new SignUpResponse(accessToken);
  }
}
