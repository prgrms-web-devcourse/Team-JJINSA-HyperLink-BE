package com.hyperlink.server.domain.auth.token;

import com.hyperlink.server.domain.auth.token.exception.TokenExpiredException;
import com.hyperlink.server.domain.auth.token.exception.TokenInvalidFormatException;
import com.hyperlink.server.domain.auth.token.exception.TokenNotExistsException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthTokenExtractor {

  private static final int TOKEN_FORMAT_LENGTH = 2;
  private static final int TOKEN_TYPE_INDEX = 0;
  private static final int TOKEN_VALUE_INDEX = 1;
  private static final String TOKEN_TYPE = "Bearer";

  private final SecretKey SECRET_KEY;

  public AuthTokenExtractor(@Value("${accessToken.secretKey}") String secretKey) {
    this.SECRET_KEY = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
  }

  public String extractToken(final String authorizationHeader) {
    if (authorizationHeader == null) {
      throw new TokenNotExistsException();
    }
    final String[] splitHeaders = authorizationHeader.split(" ");
    if (splitHeaders.length != TOKEN_FORMAT_LENGTH
        || !splitHeaders[TOKEN_TYPE_INDEX].equalsIgnoreCase(
        TOKEN_TYPE)) {
      log.info(
          "##### extractToken() if(splitHeaders.length != TOKEN_FORMAT_LENGTH || !splitHeaders[TOKEN_TYPE_INDEX].equalsIgnoreCase(TOKEN_TYPE))!");
      throw new TokenInvalidFormatException();
    }
    return splitHeaders[TOKEN_VALUE_INDEX];
  }

  public Optional<Long> extractMemberId(final String accessToken) {
    try {
      if (accessToken == null || accessToken.isBlank()) {
        return Optional.empty();
      }
      String memberId = Jwts.parserBuilder()
          .setSigningKey(SECRET_KEY)
          .build()
          .parseClaimsJws(accessToken)
          .getBody()
          .getSubject();
      return Optional.of(Long.parseLong(memberId));
    } catch (final JwtException e) {
      log.info("###### 토큰이 존재하지만 parsing에서 문제발생!");
      return Optional.empty();
//      throw new TokenInvalidFormatException();
    }
  }

  public void validateExpiredToken(final String accessToken) {
    try {
      Date expiration = Jwts.parserBuilder().setSigningKey(SECRET_KEY).build()
          .parseClaimsJws(accessToken).getBody().getExpiration();
      if (expiration.before(new Date())) {
        throw new TokenExpiredException();
      }
    } catch (ExpiredJwtException e) {
      throw new TokenExpiredException();
    } catch (JwtException e) {
      throw new TokenInvalidFormatException();
    }
  }
}
