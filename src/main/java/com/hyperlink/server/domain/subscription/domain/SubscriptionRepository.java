package com.hyperlink.server.domain.subscription.domain;

import com.hyperlink.server.domain.subscription.domain.entity.Subscription;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  boolean existsByMemberIdAndCreatorId(Long memberId, Long creatorId);
  void deleteByMemberIdAndCreatorId(Long memberId, Long creatorId);
}
