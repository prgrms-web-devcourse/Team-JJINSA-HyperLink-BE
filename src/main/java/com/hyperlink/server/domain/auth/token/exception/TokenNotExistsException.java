package com.hyperlink.server.domain.auth.token.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class TokenNotExistsException extends BusinessException {

  private static final String MESSAGE = "토큰 값이 존재하지 않습니다.";
  private static final HttpStatus status = HttpStatus.UNAUTHORIZED;

  public TokenNotExistsException() {
    super(MESSAGE, status);
  }
}
