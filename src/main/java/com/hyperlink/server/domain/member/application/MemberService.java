package com.hyperlink.server.domain.member.application;

import com.hyperlink.server.domain.member.domain.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class MemberService {

  private final MemberRepository memberRepository;

  public MemberService(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

  public boolean existsMemberByEmail(String email) {
    return memberRepository.existsMemberByEmail(email);
  }

}
