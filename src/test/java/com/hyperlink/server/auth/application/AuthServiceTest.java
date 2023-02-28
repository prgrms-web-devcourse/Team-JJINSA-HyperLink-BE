package com.hyperlink.server.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperlink.server.domain.auth.application.AuthService;
import com.hyperlink.server.domain.auth.dto.LoginRequest;
import com.hyperlink.server.domain.auth.dto.LoginResult;
import com.hyperlink.server.domain.auth.dto.RenewResult;
import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import com.hyperlink.server.domain.auth.token.RefreshToken;
import com.hyperlink.server.domain.auth.token.RefreshTokenRepository;
import com.hyperlink.server.domain.auth.token.exception.RefreshTokenNotExistException;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import java.util.UUID;
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
    assertThat(refreshTokenRepository.existsById(loginResult.refreshToken())).isTrue();

    authService.logout(loginResult.refreshToken());
    assertThat(refreshTokenRepository.existsById(loginResult.refreshToken())).isFalse();
  }


  @DisplayName("refreshToken을 통해 accessToken 재발급이 가능하다.")
  @Test
  void renewAccessTokenCorrectTest() {
    String email = "rldnd1234@naver.com";
    Member saveMember = memberRepository.save(
        new Member(email, "Chocho", "develop", "10", "localhost", 1995, "man"));

    RefreshToken refreshToken = refreshTokenRepository.save(
        new RefreshToken(UUID.randomUUID().toString(), saveMember.getId()));

    RenewResult renewResult = authService.renewAccessToken(refreshToken.getRefreshToken());

    assertThat(saveMember.getId()).isEqualTo(
        authTokenExtractor.extractMemberId(renewResult.accessToken()));
  }

  @DisplayName("refreshToken을 통해 accessToken 재발급이 가능하다.")
  @Test
  void renewAccessTokenInCorrectTest() {

    assertThatThrownBy(() -> {
      authService.renewAccessToken(UUID.randomUUID().toString());
    }).isInstanceOf(RefreshTokenNotExistException.class);
  }
}