package com.hyperlink.server.domain.member.dto;

public record SignUpResult(
    Long memberId,
    String accessToken,
    String refreshToken
) {

}
