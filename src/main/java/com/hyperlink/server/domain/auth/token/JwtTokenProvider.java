package com.hyperlink.server.domain.auth.token;

import com.hyperlink.server.domain.member.domain.MemberRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

  private final MemberRepository memberRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final SecretKey SECRET_KEY;
  private final int EXPIRES;

  public JwtTokenProvider(MemberRepository memberRepository,
      RefreshTokenRepository refreshTokenRepository,
      @Value("${accessToken.secretKey}") String secretKey,
      @Value("${accessToken.expires}") int expires) {

    this.memberRepository = memberRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.SECRET_KEY = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    this.EXPIRES = expires;
  }

  public String createAccessToken(final Long memberId) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + EXPIRES);

    return Jwts.builder()
        .signWith(SECRET_KEY)
        .setIssuedAt(now)
        .setExpiration(expiration)
        .setSubject(String.valueOf(memberId))
        .compact();
  }
}
