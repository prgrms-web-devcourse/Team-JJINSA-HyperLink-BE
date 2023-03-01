package com.hyperlink.server.domain.creator.application;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.exception.CategoryNotFoundException;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollRequest;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollResponse;
import com.hyperlink.server.domain.creator.exception.CreatorNotFoundException;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.domain.notRecommendCreator.domain.NotRecommendCreatorRepository;
import com.hyperlink.server.domain.notRecommendCreator.domain.entity.NotRecommendCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
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

  public NotRecommendCreator notRecommend(Long memberId, Long creatorId) {
    Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
    Creator creator = creatorRepository.findById(creatorId)
        .orElseThrow(CreatorNotFoundException::new);
    NotRecommendCreator notRecommendCreator = new NotRecommendCreator(member, creator);

    return notRecommendCreatorRepository.save(notRecommendCreator);

  }
}
