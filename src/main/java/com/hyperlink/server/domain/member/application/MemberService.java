package com.hyperlink.server.domain.member.application;

import com.hyperlink.server.domain.attentionCategory.application.AttentionCategoryService;
import com.hyperlink.server.domain.auth.token.JwtTokenProvider;
import com.hyperlink.server.domain.auth.token.RefreshToken;
import com.hyperlink.server.domain.auth.token.RefreshTokenRepository;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.member.dto.SignUpRequest;
import com.hyperlink.server.domain.member.dto.SignUpResult;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class MemberService {

  private final MemberRepository memberRepository;
  private final AttentionCategoryService attentionCategoryService;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;

  public MemberService(MemberRepository memberRepository,
      AttentionCategoryService attentionCategoryService,
      JwtTokenProvider jwtTokenProvider, RefreshTokenRepository refreshTokenRepository) {
    this.memberRepository = memberRepository;

    this.attentionCategoryService = attentionCategoryService;
    this.jwtTokenProvider = jwtTokenProvider;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  public boolean existsMemberByEmail(String email) {
    return memberRepository.existsMemberByEmail(email);
  }

  @Transactional
  public SignUpResult signUp(SignUpRequest signUpRequest) {
    Member savedMember = memberRepository.save(SignUpRequest.to(signUpRequest));
    attentionCategoryService.setAttentionCategory(savedMember, signUpRequest.attentionCategory());

    Long memberId = savedMember.getId();
    String accessToken = jwtTokenProvider.createAccessToken(memberId);
    RefreshToken refreshToken = refreshTokenRepository.save(
        new RefreshToken(UUID.randomUUID().toString(), memberId));
    return new SignUpResult(memberId, accessToken, refreshToken.getRefreshToken());
  }

}