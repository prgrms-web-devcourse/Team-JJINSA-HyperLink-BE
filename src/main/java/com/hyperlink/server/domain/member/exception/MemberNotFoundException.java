package com.hyperlink.server.domain.member.exception;

import com.hyperlink.server.global.exception.BusinessException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MemberNotFoundException extends BusinessException {

  private static final String MESSAGE = "해당하는 멤버를 찾을 수 없습니다.";
  private static final HttpStatus status = HttpStatus.NOT_FOUND;

  public MemberNotFoundException() {
    super(MESSAGE, status);
  }

}
