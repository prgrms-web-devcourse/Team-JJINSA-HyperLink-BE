package com.hyperlink.server.domain.subscription.domain;

import com.hyperlink.server.domain.subscription.domain.entity.Subscription;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  boolean existsByMemberIdAndCreatorId(Long memberId, Long creatorId);
  void deleteByMemberIdAndCreatorId(Long memberId, Long creatorId);

  @Query("select sub.creator.id from Subscription sub where sub.member.id = :memberId")
  List<Long> findCreatorIdByMemberId(@Param("memberId") Long memberId);
}
