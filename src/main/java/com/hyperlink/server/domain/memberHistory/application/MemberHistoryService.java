package com.hyperlink.server.domain.memberHistory.application;

import com.hyperlink.server.domain.common.ContentDtoFactoryService;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.dto.GetContentsCommonResponse;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.domain.memberHistory.domain.entity.MemberHistory;
import com.hyperlink.server.domain.memberHistory.domain.MemberHistoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberHistoryService {

  private final ContentDtoFactoryService contentDtoFactoryService;
  private final MemberHistoryRepository memberHistoryRepository;
  private final MemberRepository memberRepository;
  private final ContentRepository contentRepository;

  public void insertMemberHistory(Long memberId, Long contentId, boolean isSearch) {
    Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
    Content content = contentRepository.findById(contentId)
        .orElseThrow(ContentNotFoundException::new);

    MemberHistory memberHistory = new MemberHistory(member, content, isSearch);
    memberHistoryRepository.save(memberHistory);
  }

  public GetContentsCommonResponse getAllHistory(Long memberId, Pageable pageable) {
    Slice<MemberHistory> findMemberHistory = memberHistoryRepository.findSliceByMemberId(memberId,
        pageable);
    List<Content> contents = findMemberHistory.get().map(MemberHistory::getContent)
        .toList();

    return contentDtoFactoryService.createContentResponses(memberId, contents,
        findMemberHistory.hasNext());
  }
}
