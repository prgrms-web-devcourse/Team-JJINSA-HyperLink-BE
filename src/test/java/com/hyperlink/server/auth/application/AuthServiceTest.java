package com.hyperlink.server.auth.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperlink.server.domain.auth.application.AuthService;
import com.hyperlink.server.domain.auth.dto.LoginRequest;
import com.hyperlink.server.domain.auth.dto.LoginResult;
import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import com.hyperlink.server.domain.auth.token.RefreshTokenRepository;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class AuthServiceTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private AuthTokenExtractor authTokenExtractor;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @DisplayName("로그인이 가능하다.")
  @Test
  void loginTest() {
    String email = "rldnd1234@naver.com";
    Member saveMember = memberRepository.save(
        new Member(email, "Chocho", "develop", "10", "localhost", 1995, "man"));

    LoginRequest loginRequest = new LoginRequest(email);
    LoginResult loginResult = authService.login(loginRequest);
    String accessToken = loginResult.accessToken();
    assertThat(authTokenExtractor.extractMemberId(accessToken))
        .isEqualTo(saveMember.getId());
    assertThat(refreshTokenRepository.existsById(loginResult.refreshToken())).isTrue();
  }

  @DisplayName("로그아웃이 가능하다.")
  @Test
  void logoutTest() {
    String email = "rldnd1234@naver.com";
    Member saveMember = memberRepository.save(
        new Member(email, "Chocho", "develop", "10", "localhost", 1995, "man"));

    LoginRequest loginRequest = new LoginRequest(email);
    LoginResult loginResult = authService.login(loginRequest);
    String accessToken = loginResult.accessToken();
    assertThat(refreshTokenRepository.existsById(loginResult.refreshToken())).isTrue();

    authService.logout(loginResult.refreshToken());
    assertThat(refreshTokenRepository.existsById(loginResult.refreshToken())).isFalse();
  }

}