package com.hyperlink.server.domain.attentionCategory.domain;

import com.hyperlink.server.domain.attentionCategory.domain.entity.AttentionCategory;
import com.hyperlink.server.domain.member.domain.entity.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AttentionCategoryRepository extends JpaRepository<AttentionCategory, Long> {

  @Query("select at.category.id from AttentionCategory at where at.member.id = :memberId")
  List<Long> findAttentionCategoryIdsByMemberId(Long memberId);

  void deleteAttentionCategoriesByMember(Member member);
}
