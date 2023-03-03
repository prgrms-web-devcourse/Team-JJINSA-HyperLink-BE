package com.hyperlink.server.domain.auth.dto;

public record LoginResult(
    String accessToken,
    String refreshToken
) {

}
