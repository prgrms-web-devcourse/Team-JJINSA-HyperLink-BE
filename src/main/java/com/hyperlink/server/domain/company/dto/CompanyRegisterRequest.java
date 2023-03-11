package com.hyperlink.server.domain.company.dto;

import com.hyperlink.server.domain.company.domain.entity.Company;

public record CompanyRegisterRequest(
    String emailAddress,
    String logoImgUrl,
    String companyName
) {

  public static Company to(CompanyRegisterRequest companyRegisterRequest) {
    return new Company(companyRegisterRequest.emailAddress(), companyRegisterRequest.logoImgUrl(),
        companyRegisterRequest.companyName());
  }
}
