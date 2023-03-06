package com.hyperlink.server.subscription.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.hyperlink.server.domain.subscription.domain.SubscriptionRepository;
import com.hyperlink.server.domain.subscription.dto.SubscribeResponse;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("구독 서비스 Mock 테스트")
public class SubscriptionServiceMockTest {

  @Mock
  SubscriptionRepository subscriptionRepository;
  @Mock
  MemberRepository memberRepository;
  @Mock
  CreatorRepository creatorRepository;

  @InjectMocks
  SubscriptionService subscriptionService;


  @Nested
  @DisplayName("크리에이터 구독 테스트는")
  class SubscribeTest {

    @Nested
    @DisplayName("[성공]")
    class Success {

      Member member = new Member("email", "nickname", Career.DEVELOP, CareerYear.EIGHT,
          "profileImgUrl");
      Category category = new Category("개발");
      Creator creator = new Creator("개발자", "profileImgUrl", "description", category);

      @Test
      @DisplayName("해당 크리에이터를 구독하지 않은 경우 구독한다.")
      void subscribeCreatorTest() throws Exception {
        when(subscriptionRepository.existsByMemberIdAndCreatorId(any(), any())).thenReturn(false);
        when(memberRepository.findById(any())).thenReturn(Optional.of(member));
        when(creatorRepository.findById(any())).thenReturn(Optional.of(creator));

        SubscribeResponse subscribeResponse = subscriptionService.subscribeOrUnsubscribeCreator(
            1L, 1L);

        assertTrue(subscribeResponse.isSubscribed());
        verify(subscriptionRepository, times(0)).deleteByMemberIdAndCreatorId(any(), any());
        verify(subscriptionRepository, times(1)).save(any());
      }

      @Test
      @DisplayName("해당 크리에이터를 구독한 경우 구독을 취소한다.")
      void unsubscribeCreatorTest() throws Exception {
        when(subscriptionRepository.existsByMemberIdAndCreatorId(any(), any())).thenReturn(true);
        when(memberRepository.findById(any())).thenReturn(Optional.of(member));
        when(creatorRepository.findById(any())).thenReturn(Optional.of(creator));

        SubscribeResponse subscribeResponse = subscriptionService.subscribeOrUnsubscribeCreator(
            1L, 1L);

        assertFalse(subscribeResponse.isSubscribed());
        verify(subscriptionRepository, times(1)).deleteByMemberIdAndCreatorId(any(), any());
        verify(subscriptionRepository, times(0)).save(any());
      }
    }
  }

}
