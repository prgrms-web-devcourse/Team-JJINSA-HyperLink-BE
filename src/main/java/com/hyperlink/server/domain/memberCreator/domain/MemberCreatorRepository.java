package com.hyperlink.server.domain.memberCreator.domain;

import com.hyperlink.server.domain.memberCreator.domain.entity.MemberCreator;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCreatorRepository extends JpaRepository<MemberCreator, Long> {

  List<MemberCreator> findByMemberId(Long memberId);
}
