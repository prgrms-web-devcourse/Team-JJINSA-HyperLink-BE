package com.hyperlink.server.domain.company.dto;

import javax.validation.constraints.NotBlank;

public record MailAuthVerifyRequest(
    @NotBlank String companyEmail,
    int authNumber,
    @NotBlank String logoImgUrl
) {

}
