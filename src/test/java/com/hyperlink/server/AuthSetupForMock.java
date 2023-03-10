package com.hyperlink.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import com.hyperlink.server.domain.auth.token.JwtTokenProvider;
import com.hyperlink.server.domain.member.application.MemberService;
import java.util.Optional;
import org.springframework.boot.test.mock.mockito.MockBean;

public class AuthSetupForMock {

  @MockBean
  protected AuthTokenExtractor authTokenExtractor;

  @MockBean
  protected JwtTokenProvider jwtTokenProvider;

  @MockBean
  protected MemberService memberService;

  protected String authorizationHeader = "Bearer ${ACCESS_TOKEN}";

  protected Long memberId = 1L;

  protected String accessToken = "${ACCESS_TOKEN}}";

  Optional<Long> optionalId = Optional.of(memberId);

  protected void authSetup() {
    given(authTokenExtractor.extractToken(authorizationHeader))
        .willReturn(accessToken);

    given(authTokenExtractor.extractMemberId(any()))
        .willReturn(optionalId);

  }
}
