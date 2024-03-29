package com.hyperlink.server.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import com.hyperlink.server.domain.auth.token.RefreshTokenRepository;
import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.company.domain.CompanyRepository;
import com.hyperlink.server.domain.company.domain.entity.Company;
import com.hyperlink.server.domain.member.application.MemberService;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.member.dto.MembersUpdateRequest;
import com.hyperlink.server.domain.member.dto.MembersUpdateResponse;
import com.hyperlink.server.domain.member.dto.MyPageResponse;
import com.hyperlink.server.domain.member.dto.ProfileImgRequest;
import com.hyperlink.server.domain.member.dto.SignUpRequest;
import com.hyperlink.server.domain.member.dto.SignUpResult;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class MemberServiceIntegrationTest {

  @Autowired
  private MemberService memberService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private AuthTokenExtractor authTokenExtractor;

  @Autowired
  private CompanyRepository companyRepository;

  @DisplayName("주어진 이메일정보로 가입 멤버 존재여부를 확인할 수 있다.")
  @Test
  void existsMemberByEmailTest() {
    Member saveMember = memberRepository.save(
        new Member("rldnd1234@naver.com", "Chocho", Career.DEVELOP, CareerYear.MORE_THAN_TEN,
            "localhost", 1995, "man"));

    assertThat(memberService.existsMemberByEmail(saveMember.getEmail())).isTrue();
    assertThat(memberService.existsMemberByEmail("rldnd")).isFalse();
  }

  @DisplayName("회원가입을 할 수 있다.")
  @Test
  void signUpTest() {
    Category develop = categoryRepository.save(new Category("develop"));
    Category beauty = categoryRepository.save(new Category("beauty"));

    SignUpRequest signUpRequest = new SignUpRequest("rldnd1234@naver.com", "Chocho", "develop",
        "ten", 1995, List.of("develop", "beauty"), "man");

    SignUpResult signUpResult = memberService.signUp(signUpRequest, "profileUrl");

    assertThat(memberRepository.existsById(signUpResult.memberId())).isTrue();
    assertThat(refreshTokenRepository.existsById(signUpResult.refreshToken())).isTrue();
    assertThat(authTokenExtractor.extractMemberId(signUpResult.accessToken()).get()).isEqualTo(
        signUpResult.memberId());
  }

  @DisplayName("회사 미인증 회원의 정보를 전달할 수 있다.")
  @Test
  void myPageCorrectTestV1() {
    Member saveMember = memberRepository.save(
        new Member("rldnd1234@naver.com", "Chocho", Career.DEVELOP, CareerYear.MORE_THAN_TEN,
            "localhost", 1995, "man"));

    MyPageResponse myPageResponse = memberService.myInfo(saveMember.getId());

    assertThat(myPageResponse.email()).isEqualTo(saveMember.getEmail());
    assertThat(myPageResponse.nickname()).isEqualTo(saveMember.getNickname());
    assertThat(myPageResponse.career()).isEqualTo(saveMember.getCareer().getValue());
    assertThat(myPageResponse.careerYear()).isEqualTo(saveMember.getCareerYear().getValue());
    assertThat(myPageResponse.profileUrl()).isEqualTo(saveMember.getProfileImgUrl());
    assertThat(myPageResponse.companyName()).isNull();
  }

  @DisplayName("회사 인증 회원의 정보를 전달할 수 있다.")
  @Test
  void myPageCorrectTestV2() {
    Member saveMember = memberRepository.save(
        new Member("rldnd1234@naver.com", "Chocho", Career.DEVELOP, CareerYear.MORE_THAN_TEN,
            "localhost", 1995, "man"));
    Company savedCompany = companyRepository.save(
        new Company("rldnd1234@kakao.com", "kakao"));

    saveMember.changeCompany(savedCompany);

    MyPageResponse myPageResponse = memberService.myInfo(saveMember.getId());

    assertThat(myPageResponse.email()).isEqualTo(saveMember.getEmail());
    assertThat(myPageResponse.nickname()).isEqualTo(saveMember.getNickname());
    assertThat(myPageResponse.career()).isEqualTo(saveMember.getCareer().getValue());
    assertThat(myPageResponse.careerYear()).isEqualTo(saveMember.getCareerYear().getValue());
    assertThat(myPageResponse.profileUrl()).isEqualTo(saveMember.getProfileImgUrl());
    assertThat(myPageResponse.companyName()).isEqualTo(savedCompany.getName());
  }

  @DisplayName("회원의 정보 전달시 member정보가 존재하지 않는 회원이라면 MemberNotFoundException을 던진다.")
  @Test
  void myPageInCorrectTest() {
    assertThatThrownBy(() -> memberService.myInfo(1001L)).isInstanceOf(
        MemberNotFoundException.class);
  }

  @DisplayName("프로필 정보를 변경할 수 있다.")
  @Test
  void changeProfileTest() {
    Member saveMember = memberRepository.save(
        new Member("rldnd1234@naver.com", "Chocho", Career.DEVELOP, CareerYear.MORE_THAN_TEN,
            "localhost", 1995, "man"));

    MembersUpdateRequest membersUpdateRequest = new MembersUpdateRequest("master", "develop",
        "one");

    MembersUpdateResponse membersUpdateResponse = memberService.changeProfile(saveMember.getId(),
        membersUpdateRequest);

    assertThat(membersUpdateResponse.nickname()).isEqualTo(membersUpdateRequest.nickname());
    assertThat(membersUpdateResponse.career()).isEqualTo(membersUpdateRequest.career());
    assertThat(membersUpdateResponse.careerYear()).isEqualTo(membersUpdateRequest.careerYear());
  }

  @DisplayName("프로필 정보 변경시 해당 member를 찾을 수없다면 MemberNotFoundException을 던진다.")
  @Test
  void changeProfileInCorrectTest() {
    MembersUpdateRequest membersUpdateRequest = new MembersUpdateRequest("master", "develop",
        "one");

    assertThatThrownBy(
        () -> memberService.changeProfile(123444L, membersUpdateRequest)).isInstanceOf(
        MemberNotFoundException.class);
  }

  @DisplayName("프로필 이미지를 변경할 수 있다.")
  @Test
  void changeProfileImgTest() {

    String priorImgUrl = "localhost";
    String changeImgUrl = "profileImgUrl";
    Member saveMember = memberRepository.save(
        new Member("rldnd1234@gmail.com", "Chocho", Career.DEVELOP, CareerYear.MORE_THAN_TEN,
            priorImgUrl, 1995, "man"));

    ProfileImgRequest profileImgRequest = new ProfileImgRequest(changeImgUrl);
    memberService.changeProfileImg(saveMember.getId(), profileImgRequest);

    memberService.changeProfileImg(saveMember.getId(), profileImgRequest);

    Member foundMember = memberRepository.findById(saveMember.getId())
        .orElseThrow(MemberNotFoundException::new);

    assertThat(foundMember.getProfileImgUrl()).isEqualTo(changeImgUrl);
  }

  @DisplayName("프로필 이미지 변경시 해당 member를 찾을 수없다면 MemberNotFoundException을 던진다.")
  @Test
  void changeProfileImgInCorrectTest() {

    String changeImgUrl = "profileImgUrl";
    ProfileImgRequest profileImgRequest = new ProfileImgRequest(changeImgUrl);

    assertThatThrownBy(
        () -> memberService.changeProfileImg(123444L, profileImgRequest)).isInstanceOf(
        MemberNotFoundException.class);
  }

  @DisplayName("멤버가 어드민인지 아닌지 알 수 있다.")
  @Test
  void isAdminTest() {

    String priorImgUrl = "localhost";
    String changeImgUrl = "profileImgUrl";
    Member savedMember = memberRepository.save(
        new Member("rldnd1234@gmail.com", "Chocho", Career.DEVELOP, CareerYear.MORE_THAN_TEN,
            priorImgUrl, 1995, "man"));

    assertThat(memberService.isAdmin(savedMember.getId())).isFalse();
  }
}