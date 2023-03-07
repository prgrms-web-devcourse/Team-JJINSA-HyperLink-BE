package com.hyperlink.server.domain.company.mail;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "mailAuth", timeToLive = 3000)
public class MailAuth {

  @Id
  String email;
  Integer authNumber;

  public MailAuth(String email, Integer authNumber) {
    this.email = email;
    this.authNumber = authNumber;
  }
}
