package com.hyperlink.server.domain.auth.oauth.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class JsonProcessingCustomException extends BusinessException {

  private static final String MESSAGE = "Json 변형 과정에서 문제가 발생하였습니다.";
  private static final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

  public JsonProcessingCustomException() {
    super(MESSAGE, status);
  }
}
