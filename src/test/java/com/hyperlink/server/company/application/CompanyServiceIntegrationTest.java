package com.hyperlink.server.company.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperlink.server.domain.company.application.CompanyService;
import com.hyperlink.server.domain.company.dto.MailRequest;
import com.hyperlink.server.domain.company.exception.MailAuthInvalidException;
import com.hyperlink.server.domain.company.mail.MailAuth;
import com.hyperlink.server.domain.company.mail.MailAuthRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class CompanyServiceIntegrationTest {

  @Autowired
  private CompanyService companyService;

  @Autowired
  private MailAuthRepository mailAuthRepository;

  @DisplayName("요청된 회사이메일에 대한 내용을 redis에 저장할 수 있다.")
  @Test
  void saveMailAuthNumberTest() {
    String email = "rldnd2637@naver.com";
    int authNumber = 123456;
    MailRequest mailRequest = new MailRequest(email);
    companyService.saveMailAuthNumber(mailRequest, authNumber);

    MailAuth foundMailAuth = mailAuthRepository.findById(email)
        .orElseThrow(MailAuthInvalidException::new);

    assertThat(foundMailAuth.getCompanyEmail()).isEqualTo(email);
    assertThat(foundMailAuth.getAuthNumber()).isEqualTo(authNumber);
  }
}