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
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
      throw new TokenInvalidFormatException();
    }
    return splitHeaders[TOKEN_VALUE_INDEX];
  }

  public Long extractMemberId(final String accessToken) {
    try {
      String memberId = Jwts.parserBuilder()
          .setSigningKey(SECRET_KEY)
          .build()
          .parseClaimsJws(accessToken)
          .getBody()
          .getSubject();
      return Long.parseLong(memberId);
    } catch (final JwtException e) {
      throw new TokenInvalidFormatException();
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
    }
  }
}
