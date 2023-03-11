package com.hyperlink.server.domain.company.dto;

import javax.validation.constraints.NotBlank;

public record CompanyChangeNameRequest(
    @NotBlank String companyName
) {

}
