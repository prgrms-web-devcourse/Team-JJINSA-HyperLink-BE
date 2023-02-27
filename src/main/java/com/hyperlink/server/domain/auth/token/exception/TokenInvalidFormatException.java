package com.hyperlink.server.domain.auth.token.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class TokenInvalidFormatException extends
    BusinessException {

  private static final String MESSAGE = "토큰 형식이 유효하지 않습니다.";
  private static final HttpStatus status = HttpStatus.BAD_REQUEST;

  public TokenInvalidFormatException() {
    super(MESSAGE, status);
  }
}
