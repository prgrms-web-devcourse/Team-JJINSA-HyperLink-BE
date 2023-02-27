package com.hyperlink.server.domain.auth.token;

import com.hyperlink.server.domain.auth.token.exception.TokenInvalidFormatException;
import com.hyperlink.server.domain.auth.token.exception.TokenNotExistsException;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenExtractor {

  private static final int TOKEN_FORMAT_LENGTH = 2;
  private static final int TOKEN_TYPE_INDEX = 0;
  private static final int TOKEN_VALUE_INDEX = 1;
  private static final String TOKEN_TYPE = "Bearer ";

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
}
