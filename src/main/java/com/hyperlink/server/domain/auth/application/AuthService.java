package com.hyperlink.server.domain.auth.application;

import com.hyperlink.server.domain.attentionCategory.application.AttentionCategoryService;
import com.hyperlink.server.domain.auth.dto.LoginRequest;
import com.hyperlink.server.domain.auth.dto.LoginResult;
import com.hyperlink.server.domain.auth.token.JwtTokenProvider;
import com.hyperlink.server.domain.auth.token.RefreshToken;
import com.hyperlink.server.domain.auth.token.RefreshTokenRepository;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final MemberRepository memberRepository;
  private final AttentionCategoryService attentionCategoryService;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;

  public AuthService(MemberRepository memberRepository,
      AttentionCategoryService attentionCategoryService, JwtTokenProvider jwtTokenProvider,
      RefreshTokenRepository refreshTokenRepository) {
    this.memberRepository = memberRepository;
    this.attentionCategoryService = attentionCategoryService;
    this.jwtTokenProvider = jwtTokenProvider;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  public LoginResult login(LoginRequest loginRequest) {

    Long memberId = memberRepository.findMemberByEmail(loginRequest.email()).orElseThrow(
        MemberNotFoundException::new).getId();
    String accessToken = jwtTokenProvider.createAccessToken(memberId);
    RefreshToken savedRefreshToken = refreshTokenRepository.save(
        new RefreshToken(UUID.randomUUID().toString(), memberId));

    return new LoginResult(accessToken, savedRefreshToken.getRefreshToken());
  }
}
