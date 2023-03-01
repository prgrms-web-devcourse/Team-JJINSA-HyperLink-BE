package com.hyperlink.server.domain.member.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class SignUpTimeOutException extends BusinessException {

  private static final String MESSAGE = "회원가입 입력시간이 초과했습니다.";
  private static final HttpStatus status = HttpStatus.NOT_FOUND;

  public SignUpTimeOutException() {
    super(MESSAGE, status);
  }
}
