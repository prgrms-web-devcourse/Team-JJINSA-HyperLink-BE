package com.hyperlink.server.member.domain;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.exception.CareerYearNotFoundException;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CareerYearTest {

  @DisplayName("value값에 맞는 CareerYear를 찾을 수 있다.")
  @ParameterizedTest
  @MethodSource("StringAndCareerYearProvider")
  void selectCareerYearCorrectTest(String value, CareerYear careerYear) {
    Assertions.assertThat(CareerYear.selectCareerYear(value)).isEqualTo(careerYear);
  }

  static Stream<Arguments> StringAndCareerYearProvider() {
    return Stream.of(
        arguments("less1", CareerYear.LESS_ONE),
        arguments("1", CareerYear.ONE),
        arguments("2", CareerYear.TWO),
        arguments("3", CareerYear.THREE),
        arguments("4", CareerYear.FOUR),
        arguments("5", CareerYear.FIVE),
        arguments("6", CareerYear.SIX),
        arguments("7", CareerYear.SEVEN),
        arguments("8", CareerYear.EIGHT),
        arguments("9", CareerYear.NINE),
        arguments("10", CareerYear.TEN),
        arguments("more10", CareerYear.MORE_TEN));
  }

  @DisplayName("CareerYear에 잘못된 value값을 전달한다면, CareerYearNotFoundException을 던진다.")
  @Test
  void selectCareerInCorrectTest() {

    Assertions.assertThatThrownBy(() ->
            CareerYear.selectCareerYear("도도도"))
        .isInstanceOf(CareerYearNotFoundException.class);
  }
}