package com.hyperlink.server.domain.auth.oauth;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "googleAccessToken", timeToLive = 3000)
public class GoogleAccessToken {

  @Id
  private String googleAccessToken;
  private String email;

  public GoogleAccessToken(String googleAccessToken, String email) {
    this.googleAccessToken = googleAccessToken;
    this.email = email;
  }
}
