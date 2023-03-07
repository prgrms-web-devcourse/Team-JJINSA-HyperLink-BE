package com.hyperlink.server.domain.company.dto;

import javax.validation.constraints.NotBlank;

public record MailRequest(
    @NotBlank String companyEmail
) {

}
