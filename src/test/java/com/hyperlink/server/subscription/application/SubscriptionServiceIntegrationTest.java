package com.hyperlink.server.subscription.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.exception.CategoryNotFoundException;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.dto.GetContentsCommonResponse;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.creator.exception.CreatorNotFoundException;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.domain.subscription.application.SubscriptionService;
import com.hyperlink.server.domain.subscription.dto.SubscribeResponse;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DisplayName("구독 서비스 통합 테스트")
public class SubscriptionServiceIntegrationTest {

  @Autowired
  SubscriptionService subscriptionService;
  @Autowired
  CategoryRepository categoryRepository;
  @Autowired
  CreatorRepository creatorRepository;
  @Autowired
  MemberRepository memberRepository;
  @Autowired
  ContentRepository contentRepository;


  @Nested
  @DisplayName("크리에이터 구독 테스트는")
  class SubscribeTest {

    @Nested
    @DisplayName("[성공]")
    class Success {

      Member member;
      Creator creator;

      @BeforeEach
      void setUp() {
        Category developCategory = new Category("개발");
        categoryRepository.save(developCategory);
        creator = new Creator("개발좋아하는사람", "profileImgUrl", "description", developCategory);
        creatorRepository.save(creator);

        member = new Member("email", "nickname", Career.DEVELOP, CareerYear.EIGHT,
            "profileImgUrl");
        memberRepository.save(member);
      }

      @Test
      @DisplayName("해당 크리에이터를 구독하지 않은 경우 구독한다.")
      void subscribeCreatorTest() throws Exception {
        Member foundMember = memberRepository.findById(member.getId()).get();
        Creator foundCreator = creatorRepository.findById(creator.getId()).get();

        SubscribeResponse subscribeResponse = subscriptionService.subscribeOrUnsubscribeCreator(
            foundMember.getId(),
            foundCreator.getId());

        assertTrue(subscribeResponse.isSubscribed());
      }

      @Test
      @DisplayName("해당 크리에이터를 구독한 경우 구독을 취소한다.")
      void unsubscribeCreatorTest() throws Exception {
        Member foundMember = memberRepository.findById(member.getId()).get();
        Creator foundCreator = creatorRepository.findById(creator.getId()).get();
        subscriptionService.subscribeOrUnsubscribeCreator(foundMember.getId(),
            foundCreator.getId());

        SubscribeResponse subscribeResponse = subscriptionService.subscribeOrUnsubscribeCreator(
            foundMember.getId(),
            foundCreator.getId());

        assertFalse(subscribeResponse.isSubscribed());
      }
    }

    @Nested
    @DisplayName("[실패]")
    class Fail {

      Member member;
      Creator creator;

      @BeforeEach
      void setUp() {
        Category developCategory = new Category("개발");
        categoryRepository.save(developCategory);
        creator = new Creator("개발좋아하는사람", "profileImgUrl", "description", developCategory);
        creatorRepository.save(creator);

        member = new Member("email", "nickname", Career.DEVELOP, CareerYear.EIGHT,
            "profileImgUrl");
        memberRepository.save(member);
      }

      @Test
      @DisplayName("해당 멤버 id가 잘못된 경우 MemberNotFoundException 이 발생한다.")
      void failWithMemberNotFoundException() {
        Long memberId = -1L;

        Assertions.assertThrows(MemberNotFoundException.class,
            () -> subscriptionService.subscribeOrUnsubscribeCreator(memberId, creator.getId()));
      }

      @Test
      @DisplayName("해당 크리에이터 id가 잘못된 경우 CreatorNotFoundException 이 발생한다.")
      void failWithCreatorNotFoundException() {
        Long creatorId = -1L;

        Assertions.assertThrows(CreatorNotFoundException.class,
            () -> subscriptionService.subscribeOrUnsubscribeCreator(member.getId(), creatorId));
      }
    }

  }

  @Nested
  @DisplayName("구독피드 조회 테스트는")
  class SubscriptionContentRetrievalTest {

    Member member;
    Category developCategory;
    Category beautyCategory;
    Creator creator1;
    Creator creator2;
    Content developContent1;
    Content developContent2;
    Content developContent3;
    Content beautyContent1;
    Content beautyContent2;
    Content beautyContent3;

    @BeforeEach
    void setUp() {
      member = new Member("email", "nickname", Career.DEVELOP, CareerYear.FOUR,
          "profileImgUrl");
      developCategory = new Category("개발");
      beautyCategory = new Category("뷰티");
      creator1 = new Creator("creatorName1", "profileImgUrl", "description",
          developCategory);
      creator2 = new Creator("creatorName2", "profileImgUrl", "description",
          beautyCategory);

      memberRepository.save(member);
      categoryRepository.save(developCategory);
      categoryRepository.save(beautyCategory);
      creatorRepository.save(creator1);
      creatorRepository.save(creator2);

      developContent1 = new Content("developtitle1", "img", "link", creator1, developCategory);
      developContent2 = new Content("developtitle2", "img", "link", creator1, developCategory);
      developContent3 = new Content("developtitle3", "img", "link", creator1, developCategory);
      beautyContent1 = new Content("beautytitle1", "img", "link", creator2, beautyCategory);
      beautyContent2 = new Content("beautytitle2", "img", "link", creator2, beautyCategory);
      beautyContent3 = new Content("beautytitle3", "img", "link", creator2, beautyCategory);
      contentRepository.saveAll(
          List.of(developContent1, developContent2, developContent3, beautyContent1,
              beautyContent2, beautyContent3));

      subscriptionService.subscribeOrUnsubscribeCreator(member.getId(), creator1.getId());
      subscriptionService.subscribeOrUnsubscribeCreator(member.getId(), creator2.getId());
    }

    @Nested
    @DisplayName("성공")
    class Success {

      @Test
      @DisplayName("카테고리 별로 조회할 수 있다.")
      void retrieveByCategory() {
        String categoryName = "개발";
        int page = 0;
        int size = 10;

        GetContentsCommonResponse getContentsCommonResponse = subscriptionService.retrieveSubscribedCreatorsContentsByCategoryId(
            member.getId(), categoryName, PageRequest.of(page, size));

        assertThat(getContentsCommonResponse.contents()).hasSize(3);
        assertThat(getContentsCommonResponse.contents().get(0).title()).startsWith("develop");
        assertThat(getContentsCommonResponse.contents().get(1).title()).startsWith("develop");
        assertThat(getContentsCommonResponse.contents().get(2).title()).startsWith("develop");
      }

      @Test
      @DisplayName("전체 카테고리에 대해 조회할 수 있다.")
      void retrieveForAllCategory() {
        int page = 0;
        int size = 10;

        GetContentsCommonResponse getContentsCommonResponse = subscriptionService.retrieveSubscribedCreatorsContentsForAllCategories(
            member.getId(), PageRequest.of(page, size));

        assertThat(getContentsCommonResponse.contents()).hasSize(6);
      }
    }

    @Nested
    @DisplayName("실패")
    class Fail {

      @Test
      @DisplayName("카테고리 별로 조회 시 잘못된 카테고리가 들어오면 CategoryNotFoundException 이 발생한다.")
      void getInvalidCategoryThrowCategoryNotFoundException() {
        String categoryName = "invalidCategoryName";
        int page = 0;
        int size = 10;

        assertThrows(CategoryNotFoundException.class,
            () -> subscriptionService.retrieveSubscribedCreatorsContentsByCategoryId(member.getId(),
                categoryName, PageRequest.of(page, size)));
      }
    }
  }
}
