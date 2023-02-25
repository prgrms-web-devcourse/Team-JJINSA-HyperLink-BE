package com.hyperlink.server.domain.auth.dto;

public record GoogleProfileResult(
    String sub,
    String name,
    String given_name,
    String family_name,
    String picture,
    String email,
    boolean email_verified,
    String locale
) {

}
