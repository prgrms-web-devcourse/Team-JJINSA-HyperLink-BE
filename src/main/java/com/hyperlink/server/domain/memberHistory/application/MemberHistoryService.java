package com.hyperlink.server.domain.memberHistory.application;

import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.domain.memberHistory.domain.entity.MemberHistory;
import com.hyperlink.server.domain.memberHistory.domain.entity.MemberHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberHistoryService {

  private final MemberHistoryRepository memberHistoryRepository;
  private final MemberRepository memberRepository;
  private final ContentRepository contentRepository;

  public void insertMemberHistory(Long memberId, Long contentId) {
    Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
    Content content = contentRepository.findById(contentId)
        .orElseThrow(ContentNotFoundException::new);

    MemberHistory memberHistory = new MemberHistory(member, content);
    memberHistoryRepository.save(memberHistory);
  }

}
