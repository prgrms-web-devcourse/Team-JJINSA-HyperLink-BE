package com.hyperlink.server.domain.creator.application;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.exception.CategoryNotFoundException;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.creator.dto.CreatorAndSubscriptionCountMapper;
import com.hyperlink.server.domain.creator.dto.CreatorAdminResponse;
import com.hyperlink.server.domain.creator.dto.CreatorAdminResponses;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollRequest;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollResponse;
import com.hyperlink.server.domain.creator.dto.CreatorResponse;
import com.hyperlink.server.domain.creator.dto.CreatorsRetrievalResponse;
import com.hyperlink.server.domain.creator.dto.SubscribeFlagMapper;
import com.hyperlink.server.domain.creator.exception.CreatorNotFoundException;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.domain.notRecommendCreator.domain.NotRecommendCreatorRepository;
import com.hyperlink.server.domain.notRecommendCreator.domain.entity.NotRecommendCreator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreatorService {

  private final MemberRepository memberRepository;
  private final CreatorRepository creatorRepository;
  private final CategoryRepository categoryRepository;
  private final NotRecommendCreatorRepository notRecommendCreatorRepository;

  @Transactional
  public CreatorEnrollResponse enrollCreator(CreatorEnrollRequest creatorEnrollRequest) {
    Category category = categoryRepository.findByName(creatorEnrollRequest.categoryName())
        .orElseThrow(CategoryNotFoundException::new);
    Creator creator = CreatorEnrollRequest.toCreator(creatorEnrollRequest, category);
    Creator savedCreator = creatorRepository.save(creator);
    return CreatorEnrollResponse.from(savedCreator);
  }

  @Transactional
  public NotRecommendCreator notRecommend(Long memberId, Long creatorId) {
    Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
    Creator creator = creatorRepository.findById(creatorId)
        .orElseThrow(CreatorNotFoundException::new);
    NotRecommendCreator notRecommendCreator = new NotRecommendCreator(member, creator);

    return notRecommendCreatorRepository.save(notRecommendCreator);
  }

  @Transactional
  public void deleteCreator(Long creatorId) {
    try {
      creatorRepository.deleteById(creatorId);
    } catch (EmptyResultDataAccessException e) {
      throw new CreatorNotFoundException();
    }
  }

  public CreatorAdminResponses retrieveCreatorsForAdmin(PageRequest pageable) {
    Page<Creator> creatorPagination = creatorRepository.findCreators(pageable);
    List<Creator> creators = creatorPagination.getContent();

    List<CreatorAdminResponse> creatorAdminResponses = creators.stream()
        .map(CreatorAdminResponse::from).toList();
    return new CreatorAdminResponses(creatorAdminResponses, creatorPagination.getNumber(),
        creatorPagination.getTotalPages());
  }

  public CreatorsRetrievalResponse getCreatorsByCategory(Long memberId, String categoryName,
      Pageable pageable) {
    Slice<CreatorAndSubscriptionCountMapper> creators;
    List<CreatorResponse> creatorInfos = new ArrayList<>();
    if (categoryName.equals("all")) {
      creators = creatorRepository.findAllCreators(pageable);
      List<CreatorAndSubscriptionCountMapper> contents = creators.getContent();
      fillSubscribeStatus(memberId, contents, creatorInfos,
          creatorRepository.findCreatorIdAndSubscribeFlagByMemberId(memberId, pageable));
    } else {
      Category category = categoryRepository.findByName(categoryName)
          .orElseThrow(CategoryNotFoundException::new);
      creators = creatorRepository.findAllCreatorsByCategoryId(category.getId(), pageable);
      List<CreatorAndSubscriptionCountMapper> contents = creators.getContent();
      fillSubscribeStatus(memberId, contents, creatorInfos,
          creatorRepository.findCreatorIdAndSubscribeFlagByMemberIdAndCategoryId(memberId,
              category.getId(), pageable));
    }
    return new CreatorsRetrievalResponse(creatorInfos, creators.hasNext());
  }

  private void fillSubscribeStatus(Long memberId, List<CreatorAndSubscriptionCountMapper> contents,
      List<CreatorResponse> creatorInfos, Slice<SubscribeFlagMapper> creatorSubscriptionInfo) {
    if (memberId == null) {
      contents.forEach(content -> {
        creatorInfos.add(CreatorResponse.of(content, false));
      });
    } else {
      List<SubscribeFlagMapper> isSubscribes = creatorSubscriptionInfo.getContent();
      contents.forEach(content -> {
        fillSubscribeStatusIfCreatorIdMatch(creatorInfos, isSubscribes, content);
      });
    }
  }

  private void fillSubscribeStatusIfCreatorIdMatch(List<CreatorResponse> creatorInfos,
      List<SubscribeFlagMapper> isSubscribes,
      CreatorAndSubscriptionCountMapper content) {
    for (SubscribeFlagMapper subscribeFlagMapper : isSubscribes) {
      if (subscribeFlagMapper.getCreatorId().equals(content.getCreatorId())) {
        creatorInfos.add(CreatorResponse.of(content, subscribeFlagMapper.getIsSubscribed()));
        break;
      }
    }
  }
}
