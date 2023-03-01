package com.hyperlink.server.domain.memberHistory.domain.entity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberHistoryRepository extends JpaRepository<MemberHistory, Long> {

  List<MemberHistory> findAllByMemberId(Long memberId);
}
