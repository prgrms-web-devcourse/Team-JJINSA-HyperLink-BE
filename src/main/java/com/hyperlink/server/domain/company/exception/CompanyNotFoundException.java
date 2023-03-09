package com.hyperlink.server.domain.company.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CompanyNotFoundException extends BusinessException {

  private static final String MESSAGE = "입력하신 회사를 찾을 수 없습니다. ";
  private static final HttpStatus status = HttpStatus.NOT_FOUND;

  public CompanyNotFoundException() {
    super(MESSAGE, status);
  }

}
