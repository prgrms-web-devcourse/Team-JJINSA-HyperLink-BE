package com.hyperlink.server.domain.company.controller;

import com.hyperlink.server.domain.company.application.CompanyService;
import com.hyperlink.server.domain.company.dto.MailAuthVerifyRequest;
import com.hyperlink.server.domain.company.dto.MailRequest;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.global.config.LoginMemberId;
import java.util.Optional;
import java.util.Random;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  public void sendEmail(@RequestBody MailRequest mailRequest) {
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
}
