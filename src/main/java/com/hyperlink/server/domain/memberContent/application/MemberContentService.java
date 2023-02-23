package com.hyperlink.server.domain.memberContent.application;

import static com.hyperlink.server.domain.memberContent.domain.entity.MemberContentActionType.BOOKMARK;

import com.hyperlink.server.domain.memberContent.domain.MemberContentRepository;
import com.hyperlink.server.domain.memberContent.domain.entity.MemberContent;
import com.hyperlink.server.domain.memberContent.exception.BookmarkExistedException;
import com.hyperlink.server.domain.memberContent.exception.BookmarkNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberContentService {

  private final MemberContentRepository memberContentRepository;

  public void createBookmark(Long memberId, Long contentId) {
    if (isBookmarked(memberId, contentId)) {
      throw new BookmarkExistedException();
    } else {
      MemberContent memberContent = new MemberContent(memberId, contentId, BOOKMARK);
      memberContentRepository.save(memberContent);
    }
  }

  public void deleteBookmark(Long memberId, Long contentId) {
    memberContentRepository.findMemberContentByMemberIdAndContentIdAndType(memberId, contentId,
        BOOKMARK.getTypeNumber()).ifPresentOrElse(memberContentRepository::delete, () -> {
      throw new BookmarkNotFoundException();
    });
  }

  private boolean isBookmarked(Long memberId, Long contentId) {
    return memberContentRepository.findMemberContentByMemberIdAndContentIdAndType(
        memberId, contentId, BOOKMARK.getTypeNumber()).isPresent();
  }
}
