package com.hyperlink.server.domain.subscription.domain;

import com.hyperlink.server.domain.subscription.domain.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

}
