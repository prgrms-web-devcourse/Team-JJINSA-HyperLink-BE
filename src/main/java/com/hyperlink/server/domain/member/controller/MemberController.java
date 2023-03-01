package com.hyperlink.server.domain.member.controller;

import com.hyperlink.server.domain.attentionCategory.dto.AttentionCategoryRequest;
import com.hyperlink.server.domain.attentionCategory.dto.AttentionCategoryResponse;
import com.hyperlink.server.domain.auth.oauth.GoogleAccessToken;
import com.hyperlink.server.domain.auth.oauth.GoogleAccessTokenRepository;
import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import com.hyperlink.server.domain.auth.token.RefreshTokenCookieProvider;
import com.hyperlink.server.domain.auth.token.exception.TokenNotExistsException;
import com.hyperlink.server.domain.member.application.MemberService;
import com.hyperlink.server.domain.member.dto.MyPageResponse;
import com.hyperlink.server.domain.member.dto.SignUpRequest;
import com.hyperlink.server.domain.member.dto.SignUpResponse;
import com.hyperlink.server.domain.member.dto.SignUpResult;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.global.config.LoginMemberId;
import java.net.URI;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MemberController {

  private final AuthTokenExtractor authTokenExtractor;
  private final GoogleAccessTokenRepository googleAccessTokenRepository;
  private final MemberService memberService;
  private final RefreshTokenCookieProvider refreshTokenCookieProvider;

  public MemberController(AuthTokenExtractor authTokenExtractor,
      GoogleAccessTokenRepository googleAccessTokenRepository,
      MemberService memberService,
      RefreshTokenCookieProvider refreshTokenCookieProvider) {
    this.authTokenExtractor = authTokenExtractor;
    this.googleAccessTokenRepository = googleAccessTokenRepository;
    this.memberService = memberService;
    this.refreshTokenCookieProvider = refreshTokenCookieProvider;
  }

  @PostMapping("/members/signup")
  public ResponseEntity<SignUpResponse> signup(HttpServletRequest request,
      @RequestBody @Valid SignUpRequest signUpRequest) {

    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    String accessToken = authTokenExtractor.extractToken(authorizationHeader);
    GoogleAccessToken googleAccessToken = googleAccessTokenRepository
        .findById(accessToken)
        .orElseThrow(TokenNotExistsException::new);

    SignUpResult signUpResult = memberService.signUp(signUpRequest,
        googleAccessToken.getProfileUrl());

    googleAccessTokenRepository.deleteById(googleAccessToken.getGoogleAccessToken());

    ResponseCookie cookie = refreshTokenCookieProvider.createCookie(signUpResult.refreshToken());

    return ResponseEntity.created(URI.create("/mypage"))
        .header(HttpHeaders.SET_COOKIE, cookie.toString()).body(SignUpResponse.from(
            signUpResult.accessToken()));
  }

  @GetMapping("/members/mypage")
  @ResponseStatus(HttpStatus.OK)
  public MyPageResponse myPage(@LoginMemberId Optional<Long> optionalId) {
    log.info("#### optional: " + optionalId.isEmpty());
    Long memberId = optionalId.orElseThrow(MemberNotFoundException::new);
    return memberService.myInfo(memberId);
  }

  @PutMapping("/members/attention-category")
  @ResponseStatus(HttpStatus.OK)
  public AttentionCategoryResponse changeAttentionCategory(
      @LoginMemberId Optional<Long> optionalId,
      @RequestBody AttentionCategoryRequest attentionCategoryRequest) {
    Long memberId = optionalId.orElseThrow(MemberNotFoundException::new);
    return memberService.changeAttentionCategory(memberId, attentionCategoryRequest);
  }
}
