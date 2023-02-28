package com.hyperlink.server.domain.category.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CategoryNotFoundException extends BusinessException {

  private static final String MESSAGE = "해당하는 카테고리를 찾을 수 없습니다.";
  private static final HttpStatus status = HttpStatus.NOT_FOUND;

  public CategoryNotFoundException() {
    super(MESSAGE, status);
  }
}
