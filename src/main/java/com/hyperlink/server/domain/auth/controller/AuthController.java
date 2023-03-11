package com.hyperlink.server.domain.auth.controller;

import com.hyperlink.server.domain.auth.application.AuthService;
import com.hyperlink.server.domain.auth.dto.LoginResponse;
import com.hyperlink.server.domain.auth.dto.LoginResult;
import com.hyperlink.server.domain.auth.dto.RenewResponse;
import com.hyperlink.server.domain.auth.dto.RenewResult;
import com.hyperlink.server.domain.auth.oauth.GoogleAccessToken;
import com.hyperlink.server.domain.auth.oauth.GoogleOauthClient;
import com.hyperlink.server.domain.auth.token.RefreshTokenCookieProvider;
import com.hyperlink.server.domain.auth.token.exception.InValidAccessException;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AuthController {

  private final RefreshTokenCookieProvider refreshTokenCookieProvider;
  private final AuthService authService;
  private final GoogleOauthClient googleOauthClient;

  public AuthController(RefreshTokenCookieProvider refreshTokenCookieProvider,
      AuthService authService, GoogleOauthClient googleOauthClient) {
    this.refreshTokenCookieProvider = refreshTokenCookieProvider;
    this.authService = authService;
    this.googleOauthClient = googleOauthClient;
  }

  @PostMapping("/members/login")
  public ResponseEntity<LoginResponse> login(HttpServletRequest httpServletRequest) {
    GoogleAccessToken googleAccessToken = getGoogleAccessToken(httpServletRequest);
    LoginResult loginResult = authService.login(googleAccessToken);
    ResponseCookie cookie = refreshTokenCookieProvider.createCookie(loginResult.refreshToken());

    googleOauthClient.revokeToken(googleAccessToken.getGoogleAccessToken());
    authService.googleTokenDeleteById(googleAccessToken.getGoogleAccessToken());

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(LoginResponse.from(loginResult.admin(), loginResult.accessToken()));
  }

  private GoogleAccessToken getGoogleAccessToken(HttpServletRequest httpServletRequest) {
    String authorizationHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
    String googleAccessToken = authService.extractToken(authorizationHeader);

    return authService.googleTokenFindById(googleAccessToken);
  }

  @PostMapping("/members/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(name = "refreshToken", required = false) String refreshToken) {
    validateRefreshTokenExists(refreshToken);

    authService.logout(refreshToken);

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.createLogoutCookie().toString())
        .build();
  }

  @GetMapping("/members/access-token")
  public ResponseEntity<RenewResponse> renewTokens(
      @CookieValue(name = "refreshToken", required = false) String refreshToken) {
    validateRefreshTokenExists(refreshToken);
    RenewResult renewResult = authService.renewTokens(refreshToken);

    ResponseCookie cookie = refreshTokenCookieProvider.createCookie(renewResult.refreshToken());

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(RenewResponse.from(renewResult.admin(), renewResult.accessToken()));
  }

  private void validateRefreshTokenExists(final String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new InValidAccessException();
    }
  }
}