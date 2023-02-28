package com.hyperlink.server.member.domain;


import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.exception.CareerNotFoundException;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CareerTest {

  @DisplayName("value값에 맞는 Career를 찾을 수 있다.")
  @ParameterizedTest
  @MethodSource("stringAndCareerProvider")
  void selectCareerCorrectTest(String value, Career career) {
    Assertions.assertThat(Career.selectCareer(value)).isEqualTo(career);
  }

  static Stream<Arguments> stringAndCareerProvider() {
    return Stream.of(
        arguments("develop", Career.DEVELOP),
        arguments("beauty", Career.BEAUTY),
        arguments("finance", Career.FINANCE),
        arguments("etc", Career.ETC));
  }

  @DisplayName("잘못된 value값을 받는다면 CareerNotFoundException을 던진다.")
  @Test
  void selectCareerInCorrectTest() {

    Assertions.assertThatThrownBy(() ->
            Career.selectCareer("도도도"))
        .isInstanceOf(CareerNotFoundException.class);
  }
}