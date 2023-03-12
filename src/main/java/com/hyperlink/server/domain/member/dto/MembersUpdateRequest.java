package com.hyperlink.server.domain.member.dto;

import javax.validation.constraints.NotBlank;

public record MembersUpdateRequest(
    @NotBlank String nickname,
    @NotBlank String career,
    @NotBlank String careerYear) {

}
