package com.hyperlink.server.domain.creator.application;

import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.creator.exception.CreatorNotFoundException;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.domain.memberCreator.domain.MemberCreatorRepository;
import com.hyperlink.server.domain.memberCreator.domain.entity.MemberCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreatorService {

  private final MemberRepository memberRepository;
  private final CreatorRepository creatorRepository;
  private final MemberCreatorRepository memberCreatorRepository;

  public MemberCreator notRecommend(Long memberId, Long creatorId) {
    Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
    Creator creator = creatorRepository.findById(creatorId)
        .orElseThrow(CreatorNotFoundException::new);
    MemberCreator memberCreator = new MemberCreator(member, creator);

    return memberCreatorRepository.save(memberCreator);
  }
}
