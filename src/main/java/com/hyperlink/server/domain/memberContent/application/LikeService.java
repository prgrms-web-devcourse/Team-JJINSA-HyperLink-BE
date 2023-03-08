package com.hyperlink.server.domain.memberContent.application;

import static com.hyperlink.server.domain.memberContent.domain.entity.MemberContentActionType.LIKE;

import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.memberContent.domain.MemberContentRepository;
import com.hyperlink.server.domain.memberContent.domain.entity.MemberContent;
import com.hyperlink.server.domain.memberContent.dto.LikeClickRequest;
import com.hyperlink.server.domain.memberContent.exception.LikeExistedException;
import com.hyperlink.server.domain.memberContent.exception.LikeNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {

  private final MemberContentRepository memberContentRepository;
  private final ContentService contentService;

  @Transactional
  public void clickLike(Long memberId, Long contentId, LikeClickRequest likeClickRequest) {
    if (likeClickRequest.addLike()) {
      createLike(memberId, contentId);
      return;
    }
    deleteLike(memberId, contentId);
  }

  private void createLike(Long memberId, Long contentId) {
    contentService.addLike(contentId);
    existLike(memberId, contentId);
    memberContentRepository.save(new MemberContent(memberId, contentId, LIKE));
  }

  private void deleteLike(Long memberId, Long contentId) {
    MemberContent foundLike = memberContentRepository.findMemberContentByMemberIdAndContentIdAndType(
        memberId, contentId, LIKE.getTypeNumber()).orElseThrow(
        LikeNotFoundException::new);
    memberContentRepository.delete(foundLike);
    contentService.subTractLike(contentId);
  }

  private void existLike(Long memberId, Long contentId) {
    if (memberContentRepository.existsMemberContentByMemberIdAndContentIdAndType(memberId,
        contentId,
        LIKE.getTypeNumber())) {
      throw new LikeExistedException();
    }
  }
}
