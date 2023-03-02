package com.hyperlink.server.domain.member.controller;

import com.hyperlink.server.domain.auth.application.AuthService;
import com.hyperlink.server.domain.auth.oauth.GoogleAccessToken;
import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import com.hyperlink.server.domain.auth.token.RefreshTokenCookieProvider;
import com.hyperlink.server.domain.member.application.MemberService;
import com.hyperlink.server.domain.member.dto.SignUpRequest;
import com.hyperlink.server.domain.member.dto.SignUpResponse;
import com.hyperlink.server.domain.member.dto.SignUpResult;
import java.net.URI;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberController {

  private final AuthTokenExtractor authTokenExtractor;
  private final AuthService authService;
  private final MemberService memberService;
  private final RefreshTokenCookieProvider refreshTokenCookieProvider;

  public MemberController(AuthTokenExtractor authTokenExtractor,
      AuthService authService, MemberService memberService,
      RefreshTokenCookieProvider refreshTokenCookieProvider) {
    this.authTokenExtractor = authTokenExtractor;
    this.authService = authService;
    this.memberService = memberService;
    this.refreshTokenCookieProvider = refreshTokenCookieProvider;
  }

  @PostMapping("/members/signup")
  public ResponseEntity<SignUpResponse> signup(HttpServletRequest request,
      @RequestBody @Valid SignUpRequest signUpRequest) {
    GoogleAccessToken googleAccessToken = getGoogleAccessToken(request);

    SignUpResult signUpResult = memberService.signUp(signUpRequest,
        googleAccessToken.getProfileUrl());
    ResponseCookie cookie = refreshTokenCookieProvider.createCookie(signUpResult.refreshToken());

    authService.googleTokenDeleteById(googleAccessToken.getGoogleAccessToken());

    return ResponseEntity.created(URI.create("/mypage"))
        .header(HttpHeaders.SET_COOKIE, cookie.toString()).body(SignUpResponse.from(
            signUpResult.accessToken()));
  }

  private GoogleAccessToken getGoogleAccessToken(HttpServletRequest request) {
    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    String googleAccessToken = authService.extractToken(authorizationHeader);

    return authService.googleTokenFindById(googleAccessToken);
  }
}

