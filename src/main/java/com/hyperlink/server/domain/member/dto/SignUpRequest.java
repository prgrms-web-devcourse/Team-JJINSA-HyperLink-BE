package com.hyperlink.server.domain.member.dto;


import com.hyperlink.server.domain.member.domain.entity.Member;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record SignUpRequest(
    @NotBlank String email,
    @NotBlank @Size(max = 30) String nickname,
    @NotBlank @Size(max = 30) String career,
    @NotBlank @Size(max = 30) String careerYear,
    @NotBlank String profileUrl,
    @NotNull Integer birthYear,
    @NotNull List<String> attentionCategory
) {

  public static Member to(SignUpRequest signUpRequest) {
    return new Member(signUpRequest.email, signUpRequest.nickname,
        signUpRequest.career, signUpRequest.careerYear,
        signUpRequest.profileUrl, signUpRequest.birthYear);
  }

}
