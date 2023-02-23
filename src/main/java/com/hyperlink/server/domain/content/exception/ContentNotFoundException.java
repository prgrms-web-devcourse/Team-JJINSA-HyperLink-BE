package com.hyperlink.server.domain.content.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ContentNotFoundException extends BusinessException {

  private static final String MESSAGE = "컨텐츠를 찾을 수 없습니다.";
  private static final HttpStatus status = HttpStatus.NOT_FOUND;

  public ContentNotFoundException() {
    super(MESSAGE, status);
  }

}
