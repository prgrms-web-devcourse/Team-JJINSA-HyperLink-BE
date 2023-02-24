package com.hyperlink.server.domain.memberContent.domain;

import com.hyperlink.server.domain.memberContent.domain.entity.MemberContent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberContentRepository extends JpaRepository<MemberContent, Long> {

  Optional<MemberContent> findMemberContentByMemberIdAndContentIdAndType(Long memberId,
      Long contentId, int type);

  @Override
  void delete(MemberContent entity);
}
