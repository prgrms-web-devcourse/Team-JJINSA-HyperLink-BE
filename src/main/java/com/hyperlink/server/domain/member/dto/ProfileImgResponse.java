package com.hyperlink.server.domain.member.dto;

public record ProfileImgResponse
    (String profileUrl) {

  public static ProfileImgResponse from(String profileUrl) {
    return new ProfileImgResponse(profileUrl);
  }
}
