package com.hyperlink.server.domain.member.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CareerNotFoundException extends BusinessException {

  private static final String MESSAGE = "해당 value에 해당하는 Career를 찾을 수 없습니다.";
  private static final HttpStatus status = HttpStatus.NOT_FOUND;

  public CareerNotFoundException() {
    super(MESSAGE, status);
  }
}
