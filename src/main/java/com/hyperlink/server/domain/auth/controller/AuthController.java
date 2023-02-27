package com.hyperlink.server.domain.auth.controller;

import com.hyperlink.server.domain.auth.application.AuthService;
import com.hyperlink.server.domain.auth.dto.LoginRequest;
import com.hyperlink.server.domain.auth.dto.LoginResponse;
import com.hyperlink.server.domain.auth.dto.LoginResult;
import com.hyperlink.server.domain.auth.oauth.GoogleAccessTokenRepository;
import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import com.hyperlink.server.domain.auth.token.RefreshTokenCookieProvider;
import com.hyperlink.server.domain.auth.token.exception.TokenNotExistsException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

  private final RefreshTokenCookieProvider refreshTokenCookieProvider;
  private final AuthTokenExtractor authTokenExtractor;
  private final GoogleAccessTokenRepository googleAccessTokenRepository;
  private final AuthService authService;

  public AuthController(RefreshTokenCookieProvider refreshTokenCookieProvider,
      AuthTokenExtractor authTokenExtractor,
      GoogleAccessTokenRepository googleAccessTokenRepository, AuthService authService) {
    this.refreshTokenCookieProvider = refreshTokenCookieProvider;
    this.authTokenExtractor = authTokenExtractor;
    this.googleAccessTokenRepository = googleAccessTokenRepository;
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
    String googleAccessToken = authTokenExtractor.extractToken(authorizationHeader);

    if (!googleAccessTokenRepository.existsById(googleAccessToken)) {
      throw new TokenNotExistsException();
    }
    googleAccessTokenRepository.deleteById(googleAccessToken);
  }

}