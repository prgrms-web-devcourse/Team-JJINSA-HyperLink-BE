package com.hyperlink.server.domain.member.domain;

import com.hyperlink.server.domain.member.exception.CareerNotFoundException;
import java.util.stream.Stream;

public enum Career {
  DEVELOP("develop"),
  BEAUTY("beauty"),
  FINANCE("finance"),
  ETC("etc");

  private final String value;

  Career(String value) {
    this.value = value;
  }

  public static Career selectCareer(String value) {
    return Stream.of(values())
        .filter(career -> career.value.equals(value))
        .findFirst()
        .orElseThrow(CareerNotFoundException::new);
  }
}
