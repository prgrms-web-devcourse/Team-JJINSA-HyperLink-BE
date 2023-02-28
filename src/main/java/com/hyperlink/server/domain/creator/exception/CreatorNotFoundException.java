package com.hyperlink.server.domain.creator.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CreatorNotFoundException extends BusinessException {

  private static final String MESSAGE = "크리에이터를 찾을 수 없습니다.";
  private static final HttpStatus status = HttpStatus.NOT_FOUND;

  public CreatorNotFoundException() {
    super(MESSAGE, status);
  }
}
