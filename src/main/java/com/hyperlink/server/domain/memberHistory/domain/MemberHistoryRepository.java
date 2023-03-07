package com.hyperlink.server.domain.memberHistory.domain;

import com.hyperlink.server.domain.memberHistory.domain.entity.MemberHistory;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberHistoryRepository extends JpaRepository<MemberHistory, Long> {

  List<MemberHistory> findAllByMemberId(Long memberId);

  Slice<MemberHistory> findSliceByMemberId(Long memberId, Pageable pageable);

  Long countByCreatedAtAfter(LocalDateTime dateTime);

  @Query("select ca.name from MemberHistory mh join mh.content con join con.category ca")
  List<String> findAllByCreatedAtAfter(LocalDateTime dateTime);
}
