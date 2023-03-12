package com.hyperlink.server.domain.auth.token.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class RefreshTokenNotExistException extends BusinessException {

  private static final String MESSAGE = "RefreshToken이 존재하지 않습니다.";
  private static final HttpStatus status = HttpStatus.UNAUTHORIZED;

  public RefreshTokenNotExistException() {
    super(MESSAGE, status);
  }
}
