package com.hyperlink.server.domain.auth.token;

import org.springframework.data.repository.CrudRepository;


public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

}