package com.hyperlink.server.domain.auth.dto;

public record RenewResult(
    String accessToken,
    String refreshToken
) {

}
