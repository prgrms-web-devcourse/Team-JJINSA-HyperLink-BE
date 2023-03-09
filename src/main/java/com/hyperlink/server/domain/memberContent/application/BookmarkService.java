package com.hyperlink.server.domain.memberContent.application;

import static com.hyperlink.server.domain.memberContent.domain.entity.MemberContentActionType.BOOKMARK;

import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.dto.BookMarkedContentPageResponse;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import com.hyperlink.server.domain.memberContent.domain.MemberContentRepository;
import com.hyperlink.server.domain.memberContent.domain.entity.MemberContent;
import com.hyperlink.server.domain.memberContent.dto.BookmarkPageResponse;
import com.hyperlink.server.domain.memberContent.exception.BookmarkExistedException;
import com.hyperlink.server.domain.memberContent.exception.BookmarkNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookmarkService {

  private final MemberContentRepository memberContentRepository;
  private final ContentRepository contentRepository;

  public void createBookmark(Long memberId, Long contentId) {
    if (isBookmarked(memberId, contentId)) {
      throw new BookmarkExistedException();
    }

    Content foundContent = contentRepository.findById(contentId).orElseThrow(
        ContentNotFoundException::new);

    MemberContent memberContent = new MemberContent(memberId, foundContent, BOOKMARK);
    memberContentRepository.save(memberContent);
  }

  public void deleteBookmark(Long memberId, Long contentId) {
    Content foundContent = contentRepository.findById(contentId).orElseThrow(
        ContentNotFoundException::new);

    memberContentRepository.findMemberContentByMemberIdAndContentAndType(memberId, foundContent,
        BOOKMARK.getTypeNumber()).ifPresentOrElse(memberContentRepository::delete, () -> {
      throw new BookmarkNotFoundException();
    });
  }

  public boolean isBookmarked(Long memberId, Long contentId) {
    if (memberId == null) {
      return false;
    }

    Content foundContent = contentRepository.findById(contentId).orElseThrow(
        ContentNotFoundException::new);

    return memberContentRepository.findMemberContentByMemberIdAndContentAndType(
        memberId, foundContent, BOOKMARK.getTypeNumber()).isPresent();
  }

  public BookmarkPageResponse findBookmarkedContentForSlice(Long memberId, int page, int size) {
    Slice<MemberContent> memberContents = memberContentRepository.findMemberContentForSlice(
        memberId, PageRequest.of(page, size, Sort.by(Direction.DESC, "id")));

    List<BookMarkedContentPageResponse> contents = memberContents.stream()
        .map(memberContent -> BookMarkedContentPageResponse.from(memberContent.getContent()))
        .collect(
            Collectors.toList());

    return new BookmarkPageResponse(contents, memberContents.hasNext());
  }

}
