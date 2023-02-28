package com.hyperlink.server.domain.auth.oauth;

import org.springframework.data.repository.CrudRepository;

public interface GoogleAccessTokenRepository extends CrudRepository<GoogleAccessToken, String> {

}