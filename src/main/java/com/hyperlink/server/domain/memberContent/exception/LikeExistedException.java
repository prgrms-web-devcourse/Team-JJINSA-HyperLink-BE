package com.hyperlink.server.domain.memberContent.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class LikeExistedException extends BusinessException {

  private static final String MESSAGE = "이미 좋아요를 누르셨습니다.";
  private static final HttpStatus status = HttpStatus.BAD_REQUEST;

  public LikeExistedException() {
    super(MESSAGE, status);
  }

}
