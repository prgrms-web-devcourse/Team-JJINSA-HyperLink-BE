package com.hyperlink.server.domain.company.controller;

import com.hyperlink.server.domain.company.application.CompanyService;
import com.hyperlink.server.domain.company.dto.CompanyChangeNameRequest;
import com.hyperlink.server.domain.company.dto.CompanyPageResponse;
import com.hyperlink.server.domain.company.dto.CompanyRegisterRequest;
import com.hyperlink.server.domain.company.dto.MailAuthVerifyRequest;
import com.hyperlink.server.domain.company.dto.MailRequest;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.global.config.LoginMemberId;
import java.util.Optional;
import java.util.Random;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CompanyController {

  private final int AUTH_MAX_NUMBER = 1000000;
  private final String AUTH_MAIL_TITLE = "hyper-link 인증 번호입니다.";
  private final String AUTH_MAIL_CONTENT = "메일 인증 번호 입니다. \n 인증번호: ";

  private final CompanyService companyService;
  private final JavaMailSender javaMailSender;
  private final Random random;

  public CompanyController(CompanyService companyService, JavaMailSender javaMailSender) {
    this.companyService = companyService;
    this.javaMailSender = javaMailSender;
    this.random = new Random(System.currentTimeMillis());
  }

  @PostMapping("/companies/auth")
  @ResponseStatus(HttpStatus.OK)
  public void sendEmail(@RequestBody @Valid MailRequest mailRequest) {
    int authNumber = random.nextInt(AUTH_MAX_NUMBER);
    companyService.saveMailAuthNumber(mailRequest, authNumber);

    SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
    simpleMailMessage.setTo(mailRequest.companyEmail());

    simpleMailMessage.setSubject(AUTH_MAIL_TITLE);
    simpleMailMessage.setText(AUTH_MAIL_CONTENT + authNumber);
    javaMailSender.send(simpleMailMessage);
  }

  @PostMapping("/companies/verification")
  @ResponseStatus(HttpStatus.OK)
  public void EmailVerification(@LoginMemberId Optional<Long> optionalMemberId,
      @RequestBody MailAuthVerifyRequest mailAuthVerifyRequest) {
    Long memberId = optionalMemberId.orElseThrow(MemberNotFoundException::new);
    companyService.verifyAuthCompanyMail(memberId, mailAuthVerifyRequest);
  }

  @PutMapping("/admin/companies/{companyId}")
  @ResponseStatus(HttpStatus.OK)
  public void changeIsUsingRecommend(@PathVariable("companyId") Long companyId) {
    companyService.changeIsUsingRecommend(companyId);
  }

  @GetMapping("/admin/companies")
  @ResponseStatus(HttpStatus.OK)
  public CompanyPageResponse getCompanyPage(@RequestParam("page") int page,
      @RequestParam("size") int size) {
    return companyService.findCompaniesForPage(page, size);
  }

  @PostMapping("/admin/companies")
  @ResponseStatus(HttpStatus.OK)
  public void registerCompany(@RequestBody @Valid CompanyRegisterRequest companyRegisterRequest) {
    companyService.createCompany(companyRegisterRequest);
  }

  @PatchMapping("/admin/companies/{companyId}")
  @ResponseStatus(HttpStatus.OK)
  public void changeCompanyName(@PathVariable("companyId") Long companyId,
      @RequestBody @Valid CompanyChangeNameRequest companyChangeNameRequest) {
    companyService.changeCompanyName(companyId, companyChangeNameRequest);
  }
}
