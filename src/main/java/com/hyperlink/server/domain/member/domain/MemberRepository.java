package com.hyperlink.server.domain.member.domain;

import com.hyperlink.server.domain.member.domain.entity.Member;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Boolean existsByEmail(String email);

  Optional<Member> findByEmail(String email);

  Integer countByCreatedAtAfter(LocalDateTime dateTime);

}
