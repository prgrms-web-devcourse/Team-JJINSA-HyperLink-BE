package com.hyperlink.server.memberContent.application;

import static com.hyperlink.server.domain.memberContent.domain.entity.MemberContentActionType.BOOKMARK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.dto.BookMarkedContentPageResponse;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.memberContent.application.BookmarkService;
import com.hyperlink.server.domain.memberContent.domain.MemberContentRepository;
import com.hyperlink.server.domain.memberContent.domain.entity.MemberContent;
import com.hyperlink.server.domain.memberContent.dto.BookmarkPageResponse;
import com.hyperlink.server.domain.memberContent.exception.BookmarkExistedException;
import com.hyperlink.server.domain.memberContent.exception.BookmarkNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
@Transactional
@DisplayName("BookmarkService 통합테스트")
public class BookmarkServiceIntegrationTest {

  @Autowired
  BookmarkService bookmarkService;

  @Autowired
  MemberContentRepository memberContentRepository;
  @Autowired
  MemberRepository memberRepository;
  @Autowired
  ContentRepository contentRepository;
  @Autowired
  CategoryRepository categoryRepository;
  @Autowired
  CreatorRepository creatorRepository;

  Member member;
  Content content;
  Category newCategory;
  Creator newCreator;

  @BeforeEach
  void setUp() {
    member = new Member("email", "nickname", Career.DEVELOP, CareerYear.MORE_THAN_TEN,
        "profileImgUrl");
    Category category = new Category("개발2");
    Creator creator = new Creator("name", "profile", "description", category);
    content = new Content("title", "contentImgUrl", "link", creator, category);
    newCategory = categoryRepository.save(category);
    newCreator = creatorRepository.save(creator);
    memberRepository.save(member);
    contentRepository.save(content);
  }

  @Nested
  @DisplayName("북마크 기능을 실행하면")
  class Bookmark {

    @Nested
    @DisplayName("추가일 경우")
    class Create {

      @Test
      @DisplayName("성공 시에 memberContent DB에 북마크가 추가된다.")
      void createSuccess() {
        Long memberId = member.getId();
        Long contentId = content.getId();

        bookmarkService.createBookmark(memberId, contentId);

        MemberContent memberContent = memberContentRepository.findMemberContentByMemberIdAndContentAndType(
            memberId, content, BOOKMARK.getTypeNumber()).orElseGet(() -> null);

        assertThat(memberContent).isNotNull();
        assertThat(memberContent.getMemberId()).isEqualTo(memberId);
        assertThat(memberContent.getContent().getId()).isEqualTo(contentId);
      }

      @Test
      @DisplayName("이미 추가된 북마크일 경우 BookmarkExistedException이 발생한다.")
      void existedFail() {
        Long memberId = member.getId();
        Long contentId = content.getId();

        bookmarkService.createBookmark(memberId, contentId);

        assertThrows(BookmarkExistedException.class, () ->
            bookmarkService.createBookmark(memberId, contentId));
      }

    }

    @Nested
    @DisplayName("삭제일 경우")
    class Delete {

      @Test
      @DisplayName("존재하지 않는 북마크일 경우 BookmarkNotFoundException이 발생한다.")
      void notFoundFail() {
        Long memberId = member.getId();
        Long contentId = content.getId();

        assertThrows(BookmarkNotFoundException.class,
            () -> bookmarkService.deleteBookmark(memberId, contentId));
      }

      @Test
      @DisplayName("성공 시에 memberContent DB에 북마크가 삭제된다.")
      void deleteSuccess() {
        Long memberId = member.getId();
        Long contentId = content.getId();
        bookmarkService.createBookmark(memberId, contentId);

        bookmarkService.deleteBookmark(memberId, contentId);

        MemberContent memberContent = memberContentRepository.findMemberContentByMemberIdAndContentAndType(
            memberId, content, BOOKMARK.getTypeNumber()).orElseGet(() -> null);
        assertThat(memberContent).isNull();
      }
    }
  }
  
  @DisplayName("북마크한 컨텐츠들을 가져올 수 있다.")
  @Test
  void findBookmarkedContentForSliceTest() {
    List<Content> contents = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      Content savedContent = contentRepository.save(
          content = new Content("title" + i, "contentImgUrl" + i, "link" + i,
              newCreator, newCategory));
      contents.add(savedContent);

      bookmarkService.createBookmark(member.getId(), savedContent.getId());
    }
    BookmarkPageResponse bookmarkedContentForSlice1 = bookmarkService.findBookmarkedContentForSlice(
        member.getId(), 0, 2);
    BookmarkPageResponse bookmarkedContentForSlice2 = bookmarkService.findBookmarkedContentForSlice(
        member.getId(), 1, 2);
    BookmarkPageResponse bookmarkedContentForSlice3 = bookmarkService.findBookmarkedContentForSlice(
        member.getId(), 2, 2);

    assertThat(bookmarkedContentForSlice1.contents().size()).isEqualTo(2);
    assertThat(bookmarkedContentForSlice1.hasNext()).isTrue();
    log.info("#### 1" + bookmarkedContentForSlice1.contents().get(0));
    log.info("#### 1" + bookmarkedContentForSlice1.contents().get(1));

    assertThat(bookmarkedContentForSlice2.contents().size()).isEqualTo(2);
    assertThat(bookmarkedContentForSlice2.hasNext()).isTrue();
    log.info("#### 2" + bookmarkedContentForSlice2.contents().get(0));
    log.info("#### 2" + bookmarkedContentForSlice2.contents().get(1));

    assertThat(bookmarkedContentForSlice3.contents().size()).isEqualTo(1);
    assertThat(bookmarkedContentForSlice3.hasNext()).isFalse();
    log.info("#### 3" + bookmarkedContentForSlice3.contents().get(0));

    BookmarkPageResponse bookmarkedContentForSlice = bookmarkService.findBookmarkedContentForSlice(
        member.getId(), 0, 5);

    assertThat(bookmarkedContentForSlice.contents().size()).isEqualTo(5);
    assertThat(bookmarkedContentForSlice.hasNext()).isFalse();
    for (BookMarkedContentPageResponse b : bookmarkedContentForSlice.contents()) {
      log.info("@@@@@@ " + b);
    }
  }
}
