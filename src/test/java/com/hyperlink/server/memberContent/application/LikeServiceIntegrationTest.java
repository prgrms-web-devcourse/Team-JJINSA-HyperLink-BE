package com.hyperlink.server.memberContent.application;

import static com.hyperlink.server.domain.memberContent.domain.entity.MemberContentActionType.LIKE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.memberContent.application.LikeService;
import com.hyperlink.server.domain.memberContent.domain.MemberContentRepository;
import com.hyperlink.server.domain.memberContent.domain.entity.MemberContent;
import com.hyperlink.server.domain.memberContent.dto.LikeClickRequest;
import com.hyperlink.server.domain.memberContent.exception.LikeExistedException;
import com.hyperlink.server.domain.memberContent.exception.LikeNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DisplayName("LikeService 통합테스트")
public class LikeServiceIntegrationTest {

  @Autowired
  ContentService contentService;
  @Autowired
  LikeService likeService;
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

  @Test
  @DisplayName("좋아요 클릭시 memberContent DB에 Like가 추가되고 content의 좋아요 수가 증가한다.")
  void createSuccessTest() {
    int priorLikeCount = content.getLikeCount();
    Long memberId = member.getId();
    Long contentId = content.getId();
    LikeClickRequest likeClickRequest = new LikeClickRequest(true);
    likeService.clickLike(memberId, contentId, likeClickRequest);

    MemberContent like = memberContentRepository.findMemberContentByMemberIdAndContentIdAndType(
        memberId, contentId, LIKE.getTypeNumber()).orElseThrow(LikeNotFoundException::new);

    assertThat(like.getMemberId()).isEqualTo(memberId);
    assertThat(like.getContentId()).isEqualTo(contentId);
    assertThat(content.getLikeCount()).isEqualTo(priorLikeCount + 1);
  }

  @Test
  @DisplayName("이미 좋아요가 추가되있는 상태에서 좋아요 추가 요청이 올 경우, LikeExistedException이 발생한다.")
  void existedFailTest() {
    Long memberId = member.getId();
    Long contentId = content.getId();
    LikeClickRequest likeClickRequest = new LikeClickRequest(true);
    likeService.clickLike(memberId, contentId, likeClickRequest);
    assertThatThrownBy(
        () -> likeService.clickLike(memberId, contentId, likeClickRequest)).isInstanceOf(
        LikeExistedException.class);
  }

  @Test
  @DisplayName("좋아요 클릭시 취소요청이 온다면 DB에서 LIKE를 지우고, content의 좋아요 수가 감소한다.")
  void deleteSuccessTest() {
    Long memberId = member.getId();
    Long contentId = content.getId();
    LikeClickRequest likeClickRequestForAdd = new LikeClickRequest(true);
    LikeClickRequest likeClickRequestForDelete = new LikeClickRequest(false);
    likeService.clickLike(memberId, contentId, likeClickRequestForAdd);
    int priorLikeCount = content.getLikeCount();
    likeService.clickLike(memberId, contentId, likeClickRequestForDelete);

    boolean isPresent = memberContentRepository.existsMemberContentByMemberIdAndContentIdAndType(
        memberId, contentId, LIKE.getTypeNumber());

    assertThat(isPresent).isFalse();
    assertThat(content.getLikeCount()).isEqualTo(priorLikeCount - 1);
  }

  @Test
  @DisplayName("좋아요가 클릭되지 않는 상태에서 취소요청이 올경우, LikeNotFoundException이 발생한다.")
  void deleteFailTest() {
    Long memberId = member.getId();
    Long contentId = content.getId();

    LikeClickRequest likeClickRequest = new LikeClickRequest(false);
    assertThatThrownBy(() -> likeService.clickLike(memberId, contentId, likeClickRequest))
        .isInstanceOf(LikeNotFoundException.class);
  }
}
