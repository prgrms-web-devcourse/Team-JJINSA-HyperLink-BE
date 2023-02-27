package com.hyperlink.server.member.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperlink.server.domain.member.application.MemberService;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class MemberServiceIntegrationTest {

  @Autowired
  private MemberService memberService;

  @Autowired
  private MemberRepository memberRepository;

  @DisplayName("주어진 이메일정보로 가입 멤버 존재여부를 확인할 수 있다.")
  @Test
  public void existsMemberByEmailTest() {
    Member saveMember = memberRepository.save(
        new Member("rldnd1234@naver.com", "Chocho", "develop", "10", "localhost", 1995));

    assertThat(memberService.existsMemberByEmail(saveMember.getEmail())).isTrue();
    assertThat(memberService.existsMemberByEmail("rldnd")).isFalse();
  }

}