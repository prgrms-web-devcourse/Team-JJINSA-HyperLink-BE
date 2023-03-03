package com.hyperlink.server.domain.member.dto;


import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
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
    Integer birthYear,
    @NotNull List<String> attentionCategory,
    String gender
) {

  public static Member to(SignUpRequest signUpRequest, String profileUrl) {
    return new Member(signUpRequest.email, signUpRequest.nickname,
        Career.selectCareer(signUpRequest.career),
        CareerYear.selectCareerYear(signUpRequest.careerYear),
        profileUrl, signUpRequest.birthYear, signUpRequest.gender);
  }
}
