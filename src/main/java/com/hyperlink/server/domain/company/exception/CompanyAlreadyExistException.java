package com.hyperlink.server.domain.company.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CompanyAlreadyExistException extends BusinessException {

  private static final String MESSAGE = "이미 존재하는 회사 입니다.";
  private static final HttpStatus status = HttpStatus.BAD_REQUEST;

  public CompanyAlreadyExistException() {
    super(MESSAGE, status);
  }
}
