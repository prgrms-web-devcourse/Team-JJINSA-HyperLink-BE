package com.hyperlink.server.domain.company.dto;

import java.util.List;

public record CompanyPageResponse(
    int totalPage,
    List<CompanyResponse> companies
) {

}
