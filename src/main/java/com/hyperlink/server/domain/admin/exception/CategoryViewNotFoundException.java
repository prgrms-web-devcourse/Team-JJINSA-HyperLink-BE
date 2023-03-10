package com.hyperlink.server.domain.admin.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CategoryViewNotFoundException extends BusinessException {

  private static final String MESSAGE = "카테고리별 조회수 조회 결과를 찾을 수 없습니다.";
  private static final HttpStatus status = HttpStatus.NOT_FOUND;

  public CategoryViewNotFoundException() {
    super(MESSAGE, status);
  }
}
