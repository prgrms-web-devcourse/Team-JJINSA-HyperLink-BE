package com.hyperlink.server.domain.company.application;

import com.hyperlink.server.domain.company.domain.CompanyRepository;
import com.hyperlink.server.domain.company.domain.entity.Company;
import com.hyperlink.server.domain.company.dto.CompanyPageResponse;
import com.hyperlink.server.domain.company.dto.CompanyResponse;
import com.hyperlink.server.domain.company.dto.MailRequest;
import com.hyperlink.server.domain.company.mail.MailAuth;
import com.hyperlink.server.domain.company.mail.MailAuthRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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

  public void saveMailAuthNumber(MailRequest mailRequest, Integer authNumber) {
    mailRepository.save(
        new MailAuth(mailRequest.companyEmail(), authNumber));
  }

  public CompanyPageResponse findCompaniesForPage(int page, int size) {
    Page<Company> companies = companyRepository.findCompaniesByIsUsingRecommend(false,
        PageRequest.of(page, size, Sort.by(Direction.DESC, "id")));

    List<CompanyResponse> companyResponses = companies.stream()
        .map(company -> CompanyResponse.from(company)).collect(Collectors.toList());

    return new CompanyPageResponse(companies.getTotalPages(), companyResponses);
  }
}
