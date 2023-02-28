package com.hyperlink.server.domain.auth.token;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "refreshToken", timeToLive = 1209600)
public class RefreshToken {

  @Id
  private String refreshToken;
  private Long memberId;

  public RefreshToken(final String refreshToken, final Long memberId) {
    this.refreshToken = refreshToken;
    this.memberId = memberId;
  }
}
