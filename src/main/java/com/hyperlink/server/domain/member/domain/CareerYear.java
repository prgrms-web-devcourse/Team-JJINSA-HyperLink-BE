package com.hyperlink.server.domain.member.domain;

import com.hyperlink.server.domain.member.exception.CareerYearNotFoundException;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum CareerYear {
  LESS_ONE("less1"),
  ONE("1"),
  TWO("2"),
  THREE("3"),
  FOUR("4"),
  FIVE("5"),
  SIX("6"),
  SEVEN("7"),
  EIGHT("8"),
  NINE("9"),
  TEN("10"),
  MORE_TEN("more10");

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
