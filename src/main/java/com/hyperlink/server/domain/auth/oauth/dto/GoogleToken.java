package com.hyperlink.server.domain.auth.oauth.dto;

public record GoogleToken(
    String access_token,
    String expires_in,
    String refresh_token,
    String scope,
    String token_type,
    String id_token
) {

}
