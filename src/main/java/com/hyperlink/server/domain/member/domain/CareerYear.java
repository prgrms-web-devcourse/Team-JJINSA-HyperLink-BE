package com.hyperlink.server.domain.member.domain;

import com.hyperlink.server.domain.member.exception.CareerYearNotFoundException;
import java.util.stream.Stream;

public enum CareerYear {
  LESS_THAN_ONE("lessThanOne"),
  ONE("one"),
  TWO("two"),
  THREE("three"),
  FOUR("four"),
  FIVE("five"),
  SIX("six"),
  SEVEN("seven"),
  EIGHT("eight"),
  NINE("nine"),
  TEN("ten"),
  MORE_THAN_TEN("moreThanTen");

  private final String value;

  CareerYear(String value) {
    this.value = value;
  }

  public static CareerYear selectCareerYear(String value) {
    return Stream.of(values())
        .filter(careerYear -> careerYear.value.equals(value))
        .findFirst()
        .orElseThrow(CareerYearNotFoundException::new);
  }
}
