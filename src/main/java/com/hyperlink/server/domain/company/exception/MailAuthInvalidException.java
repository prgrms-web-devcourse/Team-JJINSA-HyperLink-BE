package com.hyperlink.server.domain.company.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class MailAuthInvalidException extends BusinessException {

  private static final String MESSAGE = "요청된 승인번호가 유효하지 않습니다. ";
  private static final HttpStatus status = HttpStatus.BAD_REQUEST;

  public MailAuthInvalidException() {
    super(MESSAGE, status);
  }
}
