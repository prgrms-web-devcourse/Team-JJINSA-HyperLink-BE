package com.hyperlink.server.domain.auth.dto;

public record RenewResult(
    Boolean admin,
    String accessToken,
    String refreshToken
) {

}
