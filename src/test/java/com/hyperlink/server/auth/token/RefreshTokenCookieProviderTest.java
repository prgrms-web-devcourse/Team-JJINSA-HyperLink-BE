package com.hyperlink.server.auth.token;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperlink.server.domain.auth.token.RefreshToken;
import com.hyperlink.server.domain.auth.token.RefreshTokenCookieProvider;
import com.hyperlink.server.domain.auth.token.RefreshTokenRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseCookie;


@SpringBootTest
class RefreshTokenCookieProviderTest {

  @Autowired
  private RefreshTokenCookieProvider refreshTokenCookieProvider;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @DisplayName("refresh토큰을 가지고 쿠키를 만들 수 있다.")
  @Test
  void createCookieTest() {

    String uuid = UUID.randomUUID().toString();
    RefreshToken savedRefreshToken = refreshTokenRepository.save(
        new RefreshToken(uuid, 1L));

    ResponseCookie responseCookie = refreshTokenCookieProvider.createCookie(
        savedRefreshToken.getRefreshToken());

    assertThat(responseCookie.getValue()).isEqualTo(uuid);
    assertThat(responseCookie.isHttpOnly()).isTrue();
    assertThat(responseCookie.isSecure()).isTrue();
  }
}