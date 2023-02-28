package com.hyperlink.server.domain.auth.token;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;


public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

  Optional<RefreshToken> findByMemberId(Long memberId);

  Boolean existsByMemberId(Long memberId);

}