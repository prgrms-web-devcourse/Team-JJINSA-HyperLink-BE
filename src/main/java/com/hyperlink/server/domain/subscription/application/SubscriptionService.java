package com.hyperlink.server.domain.subscription.application;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.exception.CategoryNotFoundException;
import com.hyperlink.server.domain.common.ContentDtoFactoryService;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.dto.GetContentsCommonResponse;
import com.hyperlink.server.domain.content.infrastructure.ContentRepositoryCustom;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.creator.exception.CreatorNotFoundException;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.domain.subscription.domain.SubscriptionRepository;
import com.hyperlink.server.domain.subscription.domain.entity.Subscription;
import com.hyperlink.server.domain.subscription.dto.SubscribeResponse;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;
  private final MemberRepository memberRepository;
  private final CreatorRepository creatorRepository;
  private final CategoryRepository categoryRepository;
  private final ContentRepositoryCustom contentRepositoryCustom;
  private final ContentDtoFactoryService contentDtoFactoryService;

  @Transactional
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

  public GetContentsCommonResponse retrieveSubscribedCreatorsContentsByCategoryId(Long loginMemberId, String categoryName, Pageable pageable) {
    Category category = categoryRepository.findByName(categoryName)
        .orElseThrow(CategoryNotFoundException::new);

    List<Long> creatorIds = subscriptionRepository.findCreatorIdByMemberId(loginMemberId);
    if(creatorIds.isEmpty()) {
      return contentDtoFactoryService.createContentResponses(loginMemberId, Collections.emptyList(),
          false);
    }
    Slice<Content> contents = contentRepositoryCustom.retrieveRecentContentsSubscribedCreatorsByCategoryId(
        creatorIds, category.getId(), pageable);
    return contentDtoFactoryService.createContentResponses(loginMemberId, contents.getContent(),
        contents.hasNext());
  }

  public GetContentsCommonResponse retrieveSubscribedCreatorsContentsForAllCategories(Long loginMemberId, Pageable pageable) {
    List<Long> creatorIds = subscriptionRepository.findCreatorIdByMemberId(loginMemberId);
    if(creatorIds.isEmpty()) {
      return contentDtoFactoryService.createContentResponses(loginMemberId, Collections.emptyList(),
          false);
    }
    Slice<Content> contents = contentRepositoryCustom.retrieveRecentContentsForAllSubscribedCreators(
        creatorIds, pageable);
    return contentDtoFactoryService.createContentResponses(loginMemberId, contents.getContent(),
        contents.hasNext());
  }
}
