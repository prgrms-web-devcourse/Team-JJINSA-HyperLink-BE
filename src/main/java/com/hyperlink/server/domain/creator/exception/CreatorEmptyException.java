package com.hyperlink.server.domain.creator.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class CreatorEmptyException extends BusinessException {


  private static final String MESSAGE = "크리에이터가 존재하지 않습니다.";
  private static final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

  public CreatorEmptyException() {
    super(MESSAGE, status);
  }

}
