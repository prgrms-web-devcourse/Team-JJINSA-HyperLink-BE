package com.hyperlink.server.domain.company.mail;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "mailAuth", timeToLive = 3000)
public class MailAuth {

  @Id
  String email;
  String companyName;
  Integer authNumber;


  public MailAuth(String email, String companyName, Integer authNumber) {
    this.email = email;
    this.companyName = companyName;
    this.authNumber = authNumber;
  }
}
