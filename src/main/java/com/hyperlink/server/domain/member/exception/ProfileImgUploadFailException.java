package com.hyperlink.server.domain.member.exception;

import com.hyperlink.server.global.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ProfileImgUploadFailException extends BusinessException {

  private static final String MESSAGE = "프로필 이미지 업로드 중 문제가 발생하였습니다.";
  private static final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

  public ProfileImgUploadFailException() {
    super(MESSAGE, status);
  }
}
