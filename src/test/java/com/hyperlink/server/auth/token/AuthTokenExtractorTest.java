package com.hyperlink.server.auth.token;

import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import com.hyperlink.server.domain.auth.token.JwtTokenProvider;
import com.hyperlink.server.domain.auth.token.exception.TokenInvalidFormatException;
import com.hyperlink.server.domain.auth.token.exception.TokenNotExistsException;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class AuthTokenExtractorTest {

  @Autowired
  private AuthTokenExtractor authTokenExtractor;

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @DisplayName("accessToken 추출시 Authorizaiton 헤더가 존재하지 않는다면, TokenNotExistsException 을 던진다.")
  @Test
  void extractTokenInCorrectTestV0() {
    String accessToken = null;
    Assertions.assertThatThrownBy(() ->
        authTokenExtractor.extractToken(accessToken)
    ).isInstanceOf(TokenNotExistsException.class);
  }

  @DisplayName("accessToken 추출시 형식이 잘못되었다면, TokenInvalidFormatException을 던진다.")
  @Test
  void extractTokenInCorrectTestV1() {
    String accessToken = "1111111111111";
    Assertions.assertThatThrownBy(() ->
        authTokenExtractor.extractToken(accessToken)
    ).isInstanceOf(TokenInvalidFormatException.class);
  }

  @DisplayName("accessToken에서 memberId 추출시 인증 유저라면, Optional<Long>값을 반환한다.")
  @Test
  void extractMemberIdCorrectTest() {
    String accessToken = jwtTokenProvider.createAccessToken(1L);
    Optional<Long> result = authTokenExtractor.extractMemberId(accessToken);

    Assertions.assertThat(result.isPresent()).isTrue();
    Assertions.assertThat(result.get()).isEqualTo(1L);
  }

  @DisplayName("accessToken이 없는 미인증 유저라면 Optional.Empty() 값을 리턴한다.")
  @Test
  void extractMemberIdInCorrectTest() {

    Optional<Long> result = authTokenExtractor.extractMemberId(null);

    Assertions.assertThat(result.isPresent()).isFalse();
    Assertions.assertThat(result.isEmpty()).isTrue();
  }

  @DisplayName("accessToken이 있지만, 잘못된 값이라면, TokenInvalidFormatException을 던진다.")
  @Test
  void extractMemberIdInCorrectTestV2() {
    Assertions.assertThat(authTokenExtractor.extractMemberId("111"))
        .isEqualTo(Optional.empty());
  }

}