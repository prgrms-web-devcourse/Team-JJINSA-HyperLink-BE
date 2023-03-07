package com.hyperlink.server.domain.company.controller;

import com.hyperlink.server.domain.company.application.CompanyService;
import com.hyperlink.server.domain.company.dto.MailRequest;
import java.util.Random;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  public ResponseEntity<Void> send(@RequestBody MailRequest mailRequest) {
    // 이메일 발신될 데이터 적재
    SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
    simpleMailMessage.setTo(mailRequest.email());
    int authNumber = random.nextInt(AUTH_MAX_NUMBER);
    companyService.saveMailAuthNumber(mailRequest.email(), authNumber);

    simpleMailMessage.setSubject(AUTH_MAIL_TITLE);
    simpleMailMessage.setText(AUTH_MAIL_CONTENT + authNumber);
    javaMailSender.send(simpleMailMessage);

    return ResponseEntity.ok().build();
  }
}
