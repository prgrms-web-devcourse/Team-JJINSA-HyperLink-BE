package com.hyperlink.server.domain.company.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record MailAuthVerifyRequest(
    @NotBlank String companyEmail,
    @NotNull Integer authNumber,
    @NotBlank String logoImgUrl
) {

}
