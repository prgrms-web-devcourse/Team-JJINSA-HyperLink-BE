package com.hyperlink.server.domain.auth.application;

import com.hyperlink.server.domain.auth.dto.LoginRequest;
import com.hyperlink.server.domain.auth.dto.LoginResult;
import com.hyperlink.server.domain.auth.oauth.GoogleAccessTokenRepository;
import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
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
  private final GoogleAccessTokenRepository googleAccessTokenRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthTokenExtractor authTokenExtractor;
  private final RefreshTokenRepository refreshTokenRepository;

  public AuthService(MemberRepository memberRepository,
      GoogleAccessTokenRepository googleAccessTokenRepository, JwtTokenProvider jwtTokenProvider,
      AuthTokenExtractor authTokenExtractor, RefreshTokenRepository refreshTokenRepository) {
    this.memberRepository = memberRepository;
    this.googleAccessTokenRepository = googleAccessTokenRepository;
    this.jwtTokenProvider = jwtTokenProvider;
    this.authTokenExtractor = authTokenExtractor;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  public String extractToken(final String authorizationHeader) {
    return authTokenExtractor.extractToken(authorizationHeader);
  }


  public LoginResult login(LoginRequest loginRequest) {

    Long memberId = memberRepository.findByEmail(loginRequest.email()).orElseThrow(
        MemberNotFoundException::new).getId();
    String accessToken = jwtTokenProvider.createAccessToken(memberId);
    RefreshToken savedRefreshToken = refreshTokenRepository.save(
        new RefreshToken(UUID.randomUUID().toString(), memberId));

    return new LoginResult(accessToken, savedRefreshToken.getRefreshToken());
  }

  public void logout(String refreshToken) {
    refreshTokenRepository.deleteById(refreshToken);
  }

  public boolean googleTokenExistsById(String googleAccessToken) {
    return googleAccessTokenRepository.existsById(googleAccessToken);
  }


  public void googleTokenDeleteById(String googleAccessToken) {
    googleAccessTokenRepository.deleteById(googleAccessToken);
  }
}
