package com.hyperlink.server.domain.company.application;

import com.hyperlink.server.domain.company.domain.CompanyRepository;
import com.hyperlink.server.domain.company.domain.entity.Company;
import com.hyperlink.server.domain.company.dto.CompanyChangeNameRequest;
import com.hyperlink.server.domain.company.dto.CompanyPageResponse;
import com.hyperlink.server.domain.company.dto.CompanyResponse;
import com.hyperlink.server.domain.company.dto.MailAuthVerifyRequest;
import com.hyperlink.server.domain.company.dto.MailRequest;
import com.hyperlink.server.domain.company.exception.CompanyNotFoundException;
import com.hyperlink.server.domain.company.exception.MailAuthInvalidException;
import com.hyperlink.server.domain.company.mail.MailAuth;
import com.hyperlink.server.domain.company.mail.MailAuthRepository;
import com.hyperlink.server.domain.member.application.MemberService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

  private static final int CHANGE_STANDARD_VALUE = 1;
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


  public CompanyPageResponse findCompaniesForPage(int page, int size) {
    Page<Company> companies = companyRepository.findCompaniesByIsUsingRecommend(false,
        PageRequest.of(page, size, Sort.by(Direction.DESC, "id")));

    List<CompanyResponse> companyResponses = companies.stream()
        .map(company -> CompanyResponse.from(company)).collect(Collectors.toList());

    return new CompanyPageResponse(companies.getTotalPages(), page + CHANGE_STANDARD_VALUE,
        companyResponses);
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
            new Company(mailAddress, mailAuthVerifyRequest.logoImgUrl(),
                extractCompanyNameFromEmail(
                    mailAddress))));

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

  @Transactional
  public void changeCompanyName(Long companyId, CompanyChangeNameRequest companyChangeNameRequest) {
    Company foundCompany = companyRepository.findById(companyId)
        .orElseThrow(CompanyNotFoundException::new);
    foundCompany.changeCompanyName(companyChangeNameRequest.companyName());
  }
}
