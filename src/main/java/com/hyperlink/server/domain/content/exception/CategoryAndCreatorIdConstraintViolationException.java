package com.hyperlink.server.domain.content.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CategoryAndCreatorIdConstraintViolationException extends BusinessException {

  private static final String MESSAGE = "카테고리와 크리에이터 id의 입력조건이 잘못되었습니다.";
  private static final HttpStatus status = HttpStatus.BAD_REQUEST;

  public CategoryAndCreatorIdConstraintViolationException() {
    super(MESSAGE, status);
  }
}
