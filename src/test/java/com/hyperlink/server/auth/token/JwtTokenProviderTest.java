package com.hyperlink.server.auth.token;

import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import com.hyperlink.server.domain.auth.token.JwtTokenProvider;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class JwtTokenProviderTest {

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @Autowired
  private AuthTokenExtractor authTokenExtractor;

  @Autowired
  private MemberRepository memberRepository;

  @DisplayName("accessToken을 만들고 해당 토큰의 멤버 식별자를 추출할 수 있다.")
  @Test
  void createAccessTokenTest() {
    Member saveMember = memberRepository.save(
        new Member("rldnd1234@naver.com", "Chocho", "develop", "10", "localhost", 1995, "man"));

    String accessToken = jwtTokenProvider.createAccessToken(saveMember.getId());
    Assertions.assertThat(authTokenExtractor.extractMemberId(accessToken).get())
        .isEqualTo(saveMember.getId());
  }
}