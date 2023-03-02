package com.hyperlink.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import java.util.Optional;
import org.springframework.boot.test.mock.mockito.MockBean;

public class AuthSetupForMock {

  @MockBean
  protected AuthTokenExtractor authTokenExtractor;

  protected String authorizationHeader = "Bearer Token";

  protected Long memberId = 1L;

  protected String accessToken = "accessToken value";

  Optional<Long> optionalId = Optional.of(memberId);

  protected void authSetup() {
    given(authTokenExtractor.extractToken(authorizationHeader))
        .willReturn(accessToken);

    given(authTokenExtractor.extractMemberId(any()))
        .willReturn(optionalId);
  }
}
