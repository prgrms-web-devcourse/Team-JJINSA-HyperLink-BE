package com.hyperlink.server.token;

import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import com.hyperlink.server.domain.auth.token.exception.TokenInvalidFormatException;
import com.hyperlink.server.domain.auth.token.exception.TokenNotExistsException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuthTokenExtractorTest {

  @Autowired
  private AuthTokenExtractor authTokenExtractor;

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

  @DisplayName("accessToken에서 memberId 추출시 형식이 잘못 되었다면, TokenInvalidFormatException을 던진다.")
  @Test
  void extractMemberIdInCorrectTest() {
    String accessToken = " 1111111111111";
    Assertions.assertThatThrownBy(() ->
        authTokenExtractor.extractMemberId(accessToken)
    ).isInstanceOf(TokenInvalidFormatException.class);
  }
}