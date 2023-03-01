package com.hyperlink.server.domain.member.dto;

import com.hyperlink.server.domain.member.domain.entity.Member;

public record MyPageResponse(
    String email,
    String nickname,
    String career,
    String careerYear,
    String profileUrl
) {

  public static MyPageResponse from(Member member) {
    return new MyPageResponse(member.getEmail(), member.getNickname(),
        member.getCareer().getValue(), member.getCareerYear().getValue(),
        member.getProfileImgUrl());
  }
}
