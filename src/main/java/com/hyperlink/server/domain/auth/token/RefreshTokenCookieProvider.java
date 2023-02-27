package com.hyperlink.server.domain.auth.token;

import java.time.Duration;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseCookie.ResponseCookieBuilder;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieProvider {

  protected static final String REFRESH_TOKEN = "refreshToken";

  private final Long expiredTimeMillis = 360000L;

  public ResponseCookie createCookie(final String refreshToken) {
    return createTokenCookieBuilder(refreshToken)
        .maxAge(Duration.ofMillis(expiredTimeMillis))
        .build();
  }
  
  private ResponseCookieBuilder createTokenCookieBuilder(final String value) {
    return ResponseCookie.from(REFRESH_TOKEN, value)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .sameSite(SameSite.NONE.attributeValue());
  }
}
