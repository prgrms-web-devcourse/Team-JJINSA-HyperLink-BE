package com.hyperlink.server.domain.memberContent.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class BookmarkNotFoundException extends BusinessException {

  private static final String MESSAGE = "북마크 기록이 존재하지 않습니다.";
  private static final HttpStatus status = HttpStatus.NOT_FOUND;

  public BookmarkNotFoundException() {
    super(MESSAGE, status);
  }
}
