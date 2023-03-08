package com.hyperlink.server.memberContent.application;

import static com.hyperlink.server.domain.memberContent.domain.entity.MemberContentActionType.BOOKMARK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.memberContent.application.MemberContentService;
import com.hyperlink.server.domain.memberContent.domain.MemberContentRepository;
import com.hyperlink.server.domain.memberContent.domain.entity.MemberContent;
import com.hyperlink.server.domain.memberContent.exception.BookmarkExistedException;
import com.hyperlink.server.domain.memberContent.exception.BookmarkNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DisplayName("BookmarkService 통합테스트")
public class BookmarkServiceIntegrationTest {

  @Autowired
  MemberContentService memberContentService;
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

  @BeforeEach
  void setUp() {
    member = new Member("email", "nickname", Career.DEVELOP, CareerYear.MORE_THAN_TEN,
        "profileImgUrl");
    Category category = new Category("개발");
    Creator creator = new Creator("name", "profile", "description", category);
    content = new Content("title", "contentImgUrl", "link", creator, category);
    categoryRepository.save(category);
    creatorRepository.save(creator);
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

        memberContentService.createBookmark(memberId, contentId);

        MemberContent memberContent = memberContentRepository.findMemberContentByMemberIdAndContentIdAndType(
            memberId, contentId, BOOKMARK.getTypeNumber()).orElseGet(() -> null);

        assertThat(memberContent).isNotNull();
        assertThat(memberContent.getMemberId()).isEqualTo(memberId);
        assertThat(memberContent.getContentId()).isEqualTo(contentId);
      }

      @Test
      @DisplayName("이미 추가된 북마크일 경우 BookmarkExistedException이 발생한다.")
      void existedFail() {
        Long memberId = member.getId();
        Long contentId = content.getId();

        memberContentService.createBookmark(memberId, contentId);

        assertThrows(BookmarkExistedException.class, () ->
            memberContentService.createBookmark(memberId, contentId));
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
            () -> memberContentService.deleteBookmark(memberId, contentId));
      }

      @Test
      @DisplayName("성공 시에 memberContent DB에 북마크가 삭제된다.")
      void deleteSuccess() {
        Long memberId = member.getId();
        Long contentId = content.getId();
        memberContentService.createBookmark(memberId, contentId);

        memberContentService.deleteBookmark(memberId, contentId);

        MemberContent memberContent = memberContentRepository.findMemberContentByMemberIdAndContentIdAndType(
            memberId, contentId, BOOKMARK.getTypeNumber()).orElseGet(() -> null);
        assertThat(memberContent).isNull();
      }
    }
  }

}
