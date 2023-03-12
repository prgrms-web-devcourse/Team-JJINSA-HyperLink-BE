package com.hyperlink.server.domain.content.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidSortException extends BusinessException {

  private static final String MESSAGE = "올바르지 않은 정렬 옵션입니다.";
  private static final HttpStatus status = HttpStatus.BAD_REQUEST;


  public InvalidSortException() {
    super(MESSAGE, status);
  }
}
