package com.hyperlink.server.domain.member.domain;

import com.hyperlink.server.domain.member.domain.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Boolean existsMemberByEmail(String email);

  Optional<Member> findMemberByEmail(String email);

}
