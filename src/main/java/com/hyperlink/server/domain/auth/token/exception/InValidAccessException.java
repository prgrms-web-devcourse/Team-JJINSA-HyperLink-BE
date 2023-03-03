package com.hyperlink.server.domain.auth.token.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InValidAccessException extends BusinessException {

  private static final String MESSAGE = "잘못된 접근 입니다.";
  private static final HttpStatus status = HttpStatus.UNAUTHORIZED;

  public InValidAccessException() {
    super(MESSAGE, status);
  }
}
