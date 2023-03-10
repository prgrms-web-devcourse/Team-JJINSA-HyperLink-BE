package com.hyperlink.server.domain.member.application;

import com.hyperlink.server.domain.attentionCategory.application.AttentionCategoryService;
import com.hyperlink.server.domain.attentionCategory.domain.AttentionCategoryRepository;
import com.hyperlink.server.domain.auth.token.JwtTokenProvider;
import com.hyperlink.server.domain.auth.token.RefreshToken;
import com.hyperlink.server.domain.auth.token.RefreshTokenRepository;
import com.hyperlink.server.domain.company.domain.CompanyRepository;
import com.hyperlink.server.domain.company.domain.entity.Company;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.member.dto.MembersUpdateRequest;
import com.hyperlink.server.domain.member.dto.MembersUpdateResponse;
import com.hyperlink.server.domain.member.dto.MyPageResponse;
import com.hyperlink.server.domain.member.dto.ProfileImgRequest;
import com.hyperlink.server.domain.member.dto.SignUpRequest;
import com.hyperlink.server.domain.member.dto.SignUpResult;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
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
  private final AttentionCategoryRepository attentionCategoryRepository;
  private final CompanyRepository companyRepository;

  public MemberService(MemberRepository memberRepository,
      AttentionCategoryService attentionCategoryService,
      JwtTokenProvider jwtTokenProvider, RefreshTokenRepository refreshTokenRepository,
      AttentionCategoryRepository attentionCategoryRepository,
      CompanyRepository companyRepository) {
    this.memberRepository = memberRepository;
    this.attentionCategoryService = attentionCategoryService;
    this.jwtTokenProvider = jwtTokenProvider;
    this.refreshTokenRepository = refreshTokenRepository;
    this.attentionCategoryRepository = attentionCategoryRepository;
    this.companyRepository = companyRepository;
  }

  public boolean existsMemberByEmail(String email) {
    return memberRepository.existsByEmail(email);
  }

  @Transactional
  public SignUpResult signUp(SignUpRequest signUpRequest, String profileUrl) {
    Member savedMember = memberRepository.save(SignUpRequest.to(signUpRequest, profileUrl));
    attentionCategoryService.changeAttentionCategory(savedMember.getId(),
        signUpRequest.attentionCategory());

    Long memberId = savedMember.getId();
    String accessToken = jwtTokenProvider.createAccessToken(memberId);
    RefreshToken refreshToken = refreshTokenRepository.save(
        new RefreshToken(UUID.randomUUID().toString(), memberId));

    return new SignUpResult(memberId, accessToken, refreshToken.getRefreshToken());
  }

  public MyPageResponse myInfo(Long memberId) {
    Member foundMember = memberRepository.findById(memberId)
        .orElseThrow(MemberNotFoundException::new);

    return MyPageResponse.from(foundMember);
  }

  @Transactional
  public MembersUpdateResponse changeProfile(Long memberId,
      MembersUpdateRequest membersUpdateRequest) {
    Member foundMember = memberRepository.findById(memberId)
        .orElseThrow(MemberNotFoundException::new);
    Member changedMember = foundMember.changeMember(membersUpdateRequest.nickname(),
        Career.selectCareer(membersUpdateRequest.career()),
        CareerYear.selectCareerYear(membersUpdateRequest.careerYear()));
    return MembersUpdateResponse.from(changedMember);
  }

  @Transactional
  public void putCompanyAfterVerification(Long memberId, Company company) {
    Member foundMember = memberRepository.findById(memberId)
        .orElseThrow(MemberNotFoundException::new);
    foundMember.changeCompany(company);
  }

  @Transactional
  public void changeProfileImg(Long memberId, ProfileImgRequest profileImgRequest) {
    Member foundMember = memberRepository.findById(memberId)
        .orElseThrow(MemberNotFoundException::new);
    foundMember.changeProfileImgUrl(profileImgRequest.profileImgUrl());
  }

  public boolean isAdmin(Long memberId) {
    Member foundMember = memberRepository.findById(memberId)
        .orElseThrow(MemberNotFoundException::new);
    return foundMember.getIsAdmin();
  }
}
