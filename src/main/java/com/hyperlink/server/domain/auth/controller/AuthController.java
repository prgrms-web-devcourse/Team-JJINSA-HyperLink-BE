package com.hyperlink.server.domain.auth.controller;

import com.hyperlink.server.domain.auth.application.AuthService;
import com.hyperlink.server.domain.auth.dto.LoginRequest;
import com.hyperlink.server.domain.auth.dto.LoginResponse;
import com.hyperlink.server.domain.auth.dto.LoginResult;
import com.hyperlink.server.domain.auth.token.RefreshTokenCookieProvider;
import com.hyperlink.server.domain.auth.token.exception.InValidAccessException;
import com.hyperlink.server.domain.auth.token.exception.TokenNotExistsException;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AuthController {

  private final RefreshTokenCookieProvider refreshTokenCookieProvider;
  private final AuthService authService;

  public AuthController(RefreshTokenCookieProvider refreshTokenCookieProvider,
      AuthService authService) {
    this.refreshTokenCookieProvider = refreshTokenCookieProvider;
    this.authService = authService;
  }

  @PostMapping("/members/login")
  public ResponseEntity<LoginResponse> login(HttpServletRequest httpServletRequest,
      @RequestBody LoginRequest loginRequest) {

    checkGoogleAccessToken(httpServletRequest);
    LoginResult loginResult = authService.login(loginRequest);

    ResponseCookie cookie = refreshTokenCookieProvider.createCookie(loginResult.refreshToken());

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(LoginResponse.from(loginResult.accessToken()));
  }

  private void checkGoogleAccessToken(HttpServletRequest httpServletRequest) {
    String authorizationHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
    String googleAccessToken = authService.extractToken(authorizationHeader);

    if (!authService.googleTokenExistsById(googleAccessToken)) {
      throw new TokenNotExistsException();
    }
    authService.googleTokenDeleteById(googleAccessToken);
  }

  @PostMapping("/members/logout")
  @ResponseStatus(HttpStatus.OK)
  public void logout(@CookieValue(name = "refreshToken", required = false) String refreshToken) {

    log.info("#### refreshToken: " + refreshToken);

    validateRefreshTokenExists(refreshToken);

    authService.logout(refreshToken);
  }

  private void validateRefreshTokenExists(final String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new InValidAccessException();
    }
  }
}