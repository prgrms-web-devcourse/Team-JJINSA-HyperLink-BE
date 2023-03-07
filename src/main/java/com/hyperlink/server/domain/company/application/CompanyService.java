package com.hyperlink.server.domain.company.application;

import com.hyperlink.server.domain.company.domain.CompanyRepository;
import com.hyperlink.server.domain.company.mail.MailAuth;
import com.hyperlink.server.domain.company.mail.MailAuthRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class CompanyService {

  private final MailAuthRepository mailRepository;

  private final CompanyRepository companyRepository;

  public CompanyService(MailAuthRepository mailRepository, CompanyRepository companyRepository) {
    this.mailRepository = mailRepository;
    this.companyRepository = companyRepository;
  }

  public void saveMailAuthNumber(String email, int authNumber) {
    mailRepository.save(new MailAuth(email, authNumber));
  }
}
