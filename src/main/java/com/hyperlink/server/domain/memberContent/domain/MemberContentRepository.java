package com.hyperlink.server.domain.memberContent.domain;

import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.memberContent.domain.entity.MemberContent;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberContentRepository extends JpaRepository<MemberContent, Long> {

  Optional<MemberContent> findMemberContentByMemberIdAndContentAndType(Long memberId,
      Content content, int type);

  @Override
  void delete(MemberContent entity);

  @Query("select mc from MemberContent mc join fetch mc.content where mc.memberId = :memberId")
  Slice<MemberContent> findMemberContentForSlice(@Param("memberId") Long memberId,
      Pageable pageable);

  boolean existsMemberContentByMemberIdAndContentIdAndType(Long memberId, Long contentId, int type);

}
