package com.hyperlink.server.domain.auth.token.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends
    BusinessException {

  private static final String MESSAGE = "기간이 만료된 토큰입니다.";
  private static final HttpStatus status = HttpStatus.UNAUTHORIZED;

  public TokenExpiredException() {
    super(MESSAGE, status);
  }
}
