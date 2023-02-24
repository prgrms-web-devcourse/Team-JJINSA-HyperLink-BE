package com.hyperlink.server.domain.memberContent.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class BookmarkExistedException extends BusinessException {

  private static final String MESSAGE = "이미 존재하는 북마크입니다.";
  private static final HttpStatus status = HttpStatus.BAD_REQUEST;

  public BookmarkExistedException() {
    super(MESSAGE, status);
  }
}
