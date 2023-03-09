package com.hyperlink.server.domain.company.application;

import com.hyperlink.server.domain.company.domain.CompanyRepository;
import com.hyperlink.server.domain.company.domain.entity.Company;
import com.hyperlink.server.domain.company.dto.MailAuthVerifyRequest;
import com.hyperlink.server.domain.company.dto.MailRequest;
import com.hyperlink.server.domain.company.exception.CompanyNotFoundException;
import com.hyperlink.server.domain.company.exception.MailAuthInvalidException;
import com.hyperlink.server.domain.company.mail.MailAuth;
import com.hyperlink.server.domain.company.mail.MailAuthRepository;
import com.hyperlink.server.domain.member.application.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

  private static final int AT_INDEX = 1;
  private static final int DOT_INDEX = 0;

  private final MailAuthRepository mailRepository;
  private final MemberService memberService;
  private final CompanyRepository companyRepository;

  public CompanyService(MailAuthRepository mailRepository, MemberService memberService,
      CompanyRepository companyRepository) {
    this.mailRepository = mailRepository;
    this.memberService = memberService;
    this.companyRepository = companyRepository;
  }

  public void saveMailAuthNumber(MailRequest mailRequest, Integer authNumber) {
    mailRepository.save(
        new MailAuth(mailRequest.companyEmail(), authNumber));
  }

  @Transactional
  public void verifyAuthCompanyMail(Long memberId, MailAuthVerifyRequest mailAuthVerifyRequest) {
    MailAuth mailAuth = mailRepository.findById(mailAuthVerifyRequest.companyEmail())
        .orElseThrow(MailAuthInvalidException::new);

    if (!mailAuthVerifyRequest.authNumber().equals(mailAuth.getAuthNumber())) {
      throw new MailAuthInvalidException();
    }
    String mailAddress = extractCompanyEmail(mailAuthVerifyRequest.companyEmail());

    Company company = companyRepository.findByEmailAddress(mailAddress).orElse(
        companyRepository.save(
            new Company(mailAddress, extractCompanyNameFromEmail(
                mailAddress), mailAuthVerifyRequest.logoImgUrl())));

    memberService.putCompanyAfterVerification(memberId, company);
  }

  private String extractCompanyEmail(String companyEmail) {
    return companyEmail.split("@")[AT_INDEX];
  }

  private String extractCompanyNameFromEmail(String mailAddress) {
    return mailAddress.split("\\.")[DOT_INDEX];
  }

  @Transactional
  public void changeIsUsingRecommend(Long companyId) {
    Company foundCompany = companyRepository.findById(companyId)
        .orElseThrow(CompanyNotFoundException::new);
    foundCompany.changeIsUsingRecommend(true);
  }
}
