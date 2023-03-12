package com.hyperlink.server.domain.auth.dto;

public record LoginResult(
    Boolean admin,
    String accessToken,
    String refreshToken
) {

}
