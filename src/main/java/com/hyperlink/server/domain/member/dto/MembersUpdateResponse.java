package com.hyperlink.server.domain.member.dto;

import com.hyperlink.server.domain.member.domain.entity.Member;

public record MembersUpdateResponse(
    String nickname,
    String career,
    String careerYear
) {

  public static MembersUpdateResponse from(Member member) {
    return new MembersUpdateResponse(member.getNickname(), member.getCareer().getValue(),
        member.getCareerYear().getValue());
  }

}
