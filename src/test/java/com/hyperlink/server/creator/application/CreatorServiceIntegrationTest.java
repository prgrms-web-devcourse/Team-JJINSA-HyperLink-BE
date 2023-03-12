package com.hyperlink.server.creator.application;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.exception.CategoryNotFoundException;
import com.hyperlink.server.domain.creator.application.CreatorService;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.creator.dto.CreatorAdminResponses;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollRequest;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollResponse;
import com.hyperlink.server.domain.creator.dto.CreatorResponse;
import com.hyperlink.server.domain.creator.dto.CreatorsRetrievalResponse;
import com.hyperlink.server.domain.creator.exception.CreatorNotFoundException;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.domain.notRecommendCreator.domain.NotRecommendCreatorRepository;
import com.hyperlink.server.domain.notRecommendCreator.domain.entity.NotRecommendCreator;
import com.hyperlink.server.domain.subscription.domain.SubscriptionRepository;
import com.hyperlink.server.domain.subscription.domain.entity.Subscription;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
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
@DisplayName("CreatorService 통합 테스트")
public class CreatorServiceIntegrationTest {

  @Autowired
  CreatorService creatorService;

  @Autowired
  CategoryRepository categoryRepository;

  @Autowired
  CreatorRepository creatorRepository;

  @Autowired
  NotRecommendCreatorRepository notRecommendCreatorRepository;

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  SubscriptionRepository subscriptionRepository;

  @Nested
  @DisplayName("크리에이터 생성 메서드는")
  class CreatorEnrollTest {

    @Test
    @DisplayName("성공하면 크리에이터로 등록된다.")
    public void success() throws Exception {
      Category develop = new Category("개발");
      CreatorEnrollRequest creatorEnrollRequest = new CreatorEnrollRequest("크리에이터 이름",
          "profileImgUrl", "크리에이터입니다.", develop.getName());

      categoryRepository.save(develop);
      CreatorEnrollResponse creatorEnrollResponse = creatorService.enrollCreator(
          creatorEnrollRequest);

      Optional<Creator> foundCreator = creatorRepository.findById(creatorEnrollResponse.creatorId());
      assertThat(foundCreator).isPresent();
      assertThat(foundCreator.get().getName()).isEqualTo(creatorEnrollRequest.name());
    }

    @Test
    @DisplayName("없는 카테고리 이름으로 등록하면 CategoryNotFoundException이 발생한다.")
    public void fail() throws Exception {
      CreatorEnrollRequest creatorEnrollRequest = new CreatorEnrollRequest("크리에이터 이름",
          "profileImgUrl", "크리에이터입니다.", "개발");

      assertThatThrownBy(() -> creatorService.enrollCreator(creatorEnrollRequest))
          .isInstanceOf(CategoryNotFoundException.class);
    }
  }


  @Nested
  @DisplayName("비추천 크리에이터 추가 메소드는")
  class NotRecommend {

    Member member;
    Creator creator;
    Category category;

    @BeforeEach
    void setUp() {
      member = new Member("email", "nickname", Career.ETC, CareerYear.EIGHT, "profileImgUrl");
      memberRepository.save(member);
      category = new Category("개발");
      categoryRepository.save(category);
      creator = new Creator("name", "profileImgUrl", "descriptions", category);
      creatorRepository.save(creator);
    }

    @Test
    @DisplayName("성공하면 memberCreator 테이블에 비추천 데이터를 추가한다")
    void notRecommendTest() {
      Long memberId = member.getId();
      Long creatorId = creator.getId();

      NotRecommendCreator notRecommendCreator = creatorService.notRecommend(memberId, creatorId);

      List<NotRecommendCreator> memberNotRecommendCreators = notRecommendCreatorRepository.findByMemberId(
          memberId);

      assertThat(memberNotRecommendCreators).contains(notRecommendCreator);
    }

    @Test
    @DisplayName("DB에 존재하지 않는 memberId 요청값이 들어오면 MemberNotFoundException을 응답한다")
    void memberNotFoundExceptionTest() {
      Long memberId = 0L;
      Long creatorId = creator.getId();

      assertThrows(MemberNotFoundException.class, () ->
          creatorService.notRecommend(memberId, creatorId));
    }

    @Test
    @DisplayName("DB에 존재하지 않는 creatorId 요청값이 들어오면 MemberNotFoundException을 응답한다")
    void creatorNotFoundExceptionTest() {
      Long memberId = member.getId();
      Long creatorId = 0L;

      assertThrows(CreatorNotFoundException.class, () ->
          creatorService.notRecommend(memberId, creatorId));
    }
  }


  @Nested
  @DisplayName("크리에이터 삭제 메서드는")
  class CreatorDeleteTest {

    @Test
    @DisplayName("성공하면 크리에이터를 삭제한다.")
    public void success() throws Exception {
      Category develop = new Category("개발");
      categoryRepository.save(develop);
      Creator creator = new Creator("개발크리에이터", "profileImgUrl", "description", develop);
      creatorRepository.save(creator);

      creatorService.deleteCreator(creator.getId());

      assertFalse(creatorRepository.existsById(creator.getId()));
    }

    @Test
    @DisplayName("크리에이터가 없으면 크리에이터를 삭제하지 못하고 CreatorNotFoundException이 발생한다.")
    public void fail() throws Exception {
      Long creatorId = -1L;

      assertThrows(CreatorNotFoundException.class, () -> creatorService.deleteCreator(creatorId));
    }
  }

  @Nested
  @DisplayName("크리에이터 조회 메서드는")
  class CreatorRetrievalTest {

    Category developCategory;
    Category beautyCategory;
    Creator creator1;
    Creator creator2;
    Creator creator3;
    Member member1;
    Member member2;
    Member member3;


    @BeforeEach
    void setUp() {
      developCategory = new Category("개발");
      beautyCategory = new Category("패션");
      categoryRepository.save(developCategory);
      categoryRepository.save(beautyCategory);
      creator1 = new Creator("name1", "profileImgUrl", "description", developCategory);
      creator2 = new Creator("name2", "profileImgUrl", "description", developCategory);
      creator3 = new Creator("name3", "profileImgUrl", "description", beautyCategory);

      creatorRepository.saveAll(List.of(creator1, creator2, creator3));

      member1 = new Member("email1", "nickname1", Career.DEVELOP, CareerYear.LESS_THAN_ONE,
          "profileImgUrl");
      member2 = new Member("email2", "nickname2", Career.DEVELOP, CareerYear.LESS_THAN_ONE,
          "profileImgUrl");
      member3 = new Member("email3", "nickname3", Career.DEVELOP, CareerYear.LESS_THAN_ONE,
          "profileImgUrl");

      memberRepository.saveAll(List.of(member1, member2, member3));

      subscriptionRepository.save(new Subscription(member1, creator1));
      subscriptionRepository.save(new Subscription(member2, creator1));
      subscriptionRepository.save(new Subscription(member3, creator1));
      subscriptionRepository.save(new Subscription(member1, creator2));
      subscriptionRepository.save(new Subscription(member2, creator2));
      subscriptionRepository.save(new Subscription(member3, creator3));
    }

    @Nested
    @DisplayName("크리에이터 전체 조회 시")
    class AllCreator {
      @Nested
      @DisplayName("[로그인]")
      class IsLogin {
        @Test
        @DisplayName("로그인 한 유저가 구독했는지, 구독자 수는 몇명인지를 고려하여 전체 카테고리의 크리에이터 정보를 조회한다.")
        void retrieveAllCategoryCreator() {
          int page = 0;
          int size = 10;
          String categoryName = "all";

          CreatorsRetrievalResponse creatorsRetrievalResponse = creatorService.getCreatorsByCategory(
              member1.getId(), categoryName, PageRequest.of(page, size));

          assertThat(creatorsRetrievalResponse.creators()).hasSize(3);
          assertThat(creatorsRetrievalResponse.creators().get(0).isSubscribed()).isTrue();
          assertThat(creatorsRetrievalResponse.creators().get(0).subscriberAmount()).isEqualTo(3);
        }

        @Test
        @DisplayName("로그인 한 유저가 구독했는지, 구독자 수는 몇명인지를 고려하여 전체 카테고리의 크리에이터 정보를 조회한다.")
        void retrieveByCategoryCreator() {
          int page = 0;
          int size = 10;
          String categoryName = developCategory.getName();

          CreatorsRetrievalResponse creatorsRetrievalResponse = creatorService.getCreatorsByCategory(
              member1.getId(), categoryName, PageRequest.of(page, size));

          assertThat(creatorsRetrievalResponse.creators()).hasSize(2);
          assertThat(creatorsRetrievalResponse.creators().get(0).isSubscribed()).isTrue();
          assertThat(creatorsRetrievalResponse.creators().get(0).subscriberAmount()).isEqualTo(3);
        }
      }

      @Nested
      @DisplayName("[비 로그인]")
      class IsNotLogin {
        @Test
        @DisplayName("구독 여부는 false로, 구독자 수는 몇명인지 고려하여 전체 카테고리의 크리에이터 정보를 조회한다.")
        void retrieveAllCategoryCreatorNotLogin() {
          int page = 0;
          int size = 10;
          String categoryName = "all";

          CreatorsRetrievalResponse creatorsRetrievalResponse = creatorService.getCreatorsByCategory(
              null, categoryName, PageRequest.of(page, size));

          assertThat(creatorsRetrievalResponse.creators()).hasSize(3);
          assertThat(creatorsRetrievalResponse.creators().get(0).isSubscribed()).isFalse();
          assertThat(creatorsRetrievalResponse.creators().get(0).subscriberAmount()).isEqualTo(3);
        }

        @Test
        @DisplayName("구독 여부는 false로, 구독자 수는 몇명인지 고려하여 전체 카테고리의 크리에이터 정보를 조회한다.")
        void retrieveByCategoryCreatorNotLogin() {
          int page = 0;
          int size = 10;
          String categoryName = developCategory.getName();

          CreatorsRetrievalResponse creatorsRetrievalResponse = creatorService.getCreatorsByCategory(
              null, categoryName, PageRequest.of(page, size));

          assertThat(creatorsRetrievalResponse.creators()).hasSize(2);
          assertThat(creatorsRetrievalResponse.creators().get(0).isSubscribed()).isFalse();
          assertThat(creatorsRetrievalResponse.creators().get(0).subscriberAmount()).isEqualTo(3);
        }
      }

      @Nested
      @DisplayName("[실패]")
      class Fail {

        @Test
        @DisplayName("카테고리 입력이 잘못되면 CategoryNotFoundException 이 발생한다.")
        void invalidCategoryNameThrowsCategoryNotFoundException() {
          int page = 0;
          int size = 10;
          String invalidCategoryName = "invalid";

          assertThrows(CategoryNotFoundException.class,
              () -> creatorService.getCreatorsByCategory(null, invalidCategoryName,
                  PageRequest.of(page, size)));
        }
      }
    }

    @Nested
    @DisplayName("크리에이터 단일 조회 시")
    class OneCreator {
      @Nested
      @DisplayName("[로그인]")
      class IsLogin {
        @Test
        @DisplayName("로그인 한 유저가 구독했는지, 구독자 수는 몇명인지를 고려하여 크리에이터 정보를 조회한다.")
        void retrieveAllCategoryCreator() {
          CreatorResponse creatorResponse = creatorService.getCreatorDetail(
              member1.getId(), creator2.getId());

          assertThat(creatorResponse.isSubscribed()).isTrue();
          assertThat(creatorResponse.subscriberAmount()).isEqualTo(2);
        }
      }

      @Nested
      @DisplayName("[비 로그인]")
      class IsNotLogin {
        @Test
        @DisplayName("구독 여부는 false로, 구독자 수는 몇명인지 고려하여 크리에이터 정보를 조회한다.")
        void retrieveAllCategoryCreatorNotLogin() {
          CreatorResponse creatorResponse = creatorService.getCreatorDetail(
              null, creator2.getId());

          assertThat(creatorResponse.isSubscribed()).isFalse();
          assertThat(creatorResponse.subscriberAmount()).isEqualTo(2);
        }
      }

      @Nested
      @DisplayName("[실패]")
      class Fail {
        @Test
        @DisplayName("없는 크리에이터를 조회하려고 하면 CreatorNotFoundException을 반환한다.")
        void invalidCreatorThrowsCreatorNotFoundException() {
          Long invalidCreatorId = -1L;
          assertThrows(CreatorNotFoundException.class,
              () -> creatorService.getCreatorDetail(null, invalidCreatorId));
        }
      }
    }
  }

  @Nested
  @DisplayName("[Admin] 크리에이터 전체 조회 메소드는")
  class CreatorAdminRetrieval {
    @Test
    @DisplayName("성공하면 전체 크리에이터를 조회한.")
    public void success() throws Exception {
      Category develop = new Category("개발");
      Category beauty = new Category("패션");
      categoryRepository.save(develop);
      categoryRepository.save(beauty);
      Creator creator1 = new Creator("개발크리에이터1", "profileImgUrl1", "description", develop);
      Creator creator2 = new Creator("개발크리에이터2", "profileImgUrl2", "description", develop);
      Creator creator3 = new Creator("개발크리에이터3", "profileImgUrl3", "description", develop);
      Creator creator4 = new Creator("뷰티크리에이터1", "profileImgUrl4", "description", beauty);
      Creator creator5 = new Creator("뷰티크리에이터2", "profileImgUrl5", "description", beauty);
      creatorRepository.saveAll(List.of(creator1, creator2, creator3, creator4, creator5));

      CreatorAdminResponses creatorAdminResponses = creatorService.retrieveCreatorsForAdmin(
          PageRequest.of(0, 10));

      assertEquals(5, creatorAdminResponses.creators().size());
      assertEquals("패션", creatorAdminResponses.creators().get(4).categoryName());
    }
  }
}
