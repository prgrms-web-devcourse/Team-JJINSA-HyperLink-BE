package com.hyperlink.server.domain.memberContent.application;

import static com.hyperlink.server.domain.memberContent.domain.entity.MemberContentActionType.LIKE;

import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.memberContent.domain.MemberContentRepository;
import com.hyperlink.server.domain.memberContent.domain.entity.MemberContent;
import com.hyperlink.server.domain.memberContent.dto.LikeClickRequest;
import com.hyperlink.server.domain.memberContent.dto.LikeClickResponse;
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
  public LikeClickResponse clickLike(Long memberId, Long contentId,
      LikeClickRequest likeClickRequest) {
    if (likeClickRequest.addLike()) {
      return createLike(memberId, contentId);
    }
    return deleteLike(memberId, contentId);
  }

  private LikeClickResponse createLike(Long memberId, Long contentId) {
    int likeCount = contentService.addLike(contentId);
    existLike(memberId, contentId);
    Content foundContent = contentService.findById(contentId);
    memberContentRepository.save(new MemberContent(memberId, foundContent, LIKE));
    return new LikeClickResponse(likeCount);
  }

  private LikeClickResponse deleteLike(Long memberId, Long contentId) {
    int likeCount = contentService.subTractLike(contentId);
    Content foundContent = contentService.findById(contentId);
    MemberContent foundLike = memberContentRepository.findMemberContentByMemberIdAndContentAndType(
        memberId, foundContent, LIKE.getTypeNumber()).orElseThrow(LikeNotFoundException::new);
    memberContentRepository.delete(foundLike);
    return new LikeClickResponse(likeCount);
  }

  private void existLike(Long memberId, Long contentId) {
    if (memberContentRepository.existsMemberContentByMemberIdAndContentIdAndType(memberId,
        contentId,
        LIKE.getTypeNumber())) {
      throw new LikeExistedException();
    }
  }
}
