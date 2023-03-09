package com.hyperlink.server.domain.company.dto;

import com.hyperlink.server.domain.company.domain.entity.Company;

public record CompanyResponse(
    Long companyId,
    String companyName
) {

  public static CompanyResponse from(Company company) {
    return new CompanyResponse(company.getId(), company.getName());
  }
}
