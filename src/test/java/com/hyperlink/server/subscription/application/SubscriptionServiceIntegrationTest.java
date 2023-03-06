package com.hyperlink.server.subscription.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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


}
