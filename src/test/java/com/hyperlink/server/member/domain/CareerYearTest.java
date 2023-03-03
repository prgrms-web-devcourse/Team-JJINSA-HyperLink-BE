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
        arguments("lessThanOne", CareerYear.LESS_THAN_ONE),
        arguments("one", CareerYear.ONE),
        arguments("two", CareerYear.TWO),
        arguments("three", CareerYear.THREE),
        arguments("four", CareerYear.FOUR),
        arguments("five", CareerYear.FIVE),
        arguments("six", CareerYear.SIX),
        arguments("seven", CareerYear.SEVEN),
        arguments("eight", CareerYear.EIGHT),
        arguments("nine", CareerYear.NINE),
        arguments("ten", CareerYear.TEN),
        arguments("moreThanTen", CareerYear.MORE_THAN_TEN));
  }

  @DisplayName("CareerYear에 잘못된 value값을 전달한다면, CareerYearNotFoundException을 던진다.")
  @Test
  void selectCareerInCorrectTest() {

    Assertions.assertThatThrownBy(() ->
            CareerYear.selectCareerYear("도도도"))
        .isInstanceOf(CareerYearNotFoundException.class);
  }
}