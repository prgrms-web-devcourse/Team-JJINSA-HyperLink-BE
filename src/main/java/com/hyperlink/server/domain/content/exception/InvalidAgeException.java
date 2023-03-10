package com.hyperlink.server.domain.content.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidAgeException extends BusinessException {

  private static final String MESSAGE = "올바르지 않은 나이 타입입니다.";
  private static HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

  public InvalidAgeException() {
    super(MESSAGE, status);
  }
}
