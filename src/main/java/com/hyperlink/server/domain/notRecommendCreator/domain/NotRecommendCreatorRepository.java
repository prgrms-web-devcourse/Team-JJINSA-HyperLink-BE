package com.hyperlink.server.domain.notRecommendCreator.domain;

import com.hyperlink.server.domain.notRecommendCreator.domain.entity.NotRecommendCreator;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotRecommendCreatorRepository extends JpaRepository<NotRecommendCreator, Long> {

  List<NotRecommendCreator> findByMemberId(Long memberId);

  @Query("select nr.creator.id from NotRecommendCreator nr where nr.member.id = :memberId")
  List<Long> findNotRecommendCreatorIdByMemberId(@Param("memberId") Long memberId);
}
