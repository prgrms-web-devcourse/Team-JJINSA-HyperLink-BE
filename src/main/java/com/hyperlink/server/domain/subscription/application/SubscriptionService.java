package com.hyperlink.server.domain.subscription.application;

import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.creator.exception.CreatorNotFoundException;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.domain.subscription.domain.SubscriptionRepository;
import com.hyperlink.server.domain.subscription.domain.entity.Subscription;
import com.hyperlink.server.domain.subscription.dto.SubscribeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;
  private final MemberRepository memberRepository;
  private final CreatorRepository creatorRepository;


  public SubscribeResponse subscribeOrUnsubscribeCreator(Long loginMemberId, Long creatorId) {
    Member member = memberRepository.findById(loginMemberId)
        .orElseThrow(MemberNotFoundException::new);
    Creator creator = creatorRepository.findById(creatorId)
        .orElseThrow(CreatorNotFoundException::new);

    return subscribeOrUnsubscribeByCondition(member, creator);
  }

  private SubscribeResponse subscribeOrUnsubscribeByCondition(Member member, Creator creator) {
    boolean exist = subscriptionRepository.existsByMemberIdAndCreatorId(member.getId(),
        creator.getId());
    if(exist) {
      subscriptionRepository.deleteByMemberIdAndCreatorId(member.getId(), creator.getId());
      return SubscribeResponse.of(false);
    }
    subscriptionRepository.save(new Subscription(member, creator));
    return SubscribeResponse.of(true);
  }
}
