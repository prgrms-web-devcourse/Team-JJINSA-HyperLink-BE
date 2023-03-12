package com.hyperlink.server.creator.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.creator.dto.CreatorAndSubscriptionCountMapper;
import com.hyperlink.server.domain.creator.dto.SubscribeFlagMapper;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.subscription.domain.SubscriptionRepository;
import com.hyperlink.server.domain.subscription.domain.entity.Subscription;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class CreatorRepositoryTest {

  @Autowired
  CategoryRepository categoryRepository;

  @Autowired
  CreatorRepository creatorRepository;

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  SubscriptionRepository subscriptionRepository;

  @Test
  @DisplayName("컨텐트를 이름으로 검색할 수 있다.")
  void findContentByNameTest() {
    Category category = new Category("개발");
    Creator creator = new Creator("name", "profile", "description", category);
    categoryRepository.save(category);
    creatorRepository.save(creator);

    Creator developCreator = creatorRepository.findByName("name").get();
    assertThat(developCreator.getName()).isEqualTo("name");
  }

  @Nested
  @DisplayName("크리에이터 삭제시")
  class CreatorDeleteTest {

    @Test
    @DisplayName("존재하지 않는 크리에이터를 입력하면 EmptyResultDataAccessException 을 발생한다.")
    void deleteCreatorByIdTestEmptyResultDataAccessException() {
      assertThrows(EmptyResultDataAccessException.class, () -> creatorRepository.deleteById(44444444L));
    }

    @Test
    @DisplayName("존재하는 크리에이터를 입력하면 성공한다.")
    void deleteCreatorByIdTestSuccess() {
      Category category = new Category("개발");
      Creator creator = new Creator("name", "profile", "description", category);
      categoryRepository.save(category);
      creatorRepository.save(creator);

      creatorRepository.deleteById(creator.getId());

      assertThat(creatorRepository.existsById(creator.getId())).isFalse();
    }
  }

  @Nested
  @DisplayName("크리에이터 조회 시")
  class CreatorRetrieval {

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


    @Test
    @DisplayName("전체 카테고리에 대해 크리에이터 정보와 구독자 수를 조회할 수 있다.")
    void retrieveCreatorsForAllCategory() {
      Slice<CreatorAndSubscriptionCountMapper> creatorsSlice = creatorRepository.findAllCreators(
          PageRequest.of(0, 10));

      assertEquals(false, creatorsSlice.hasNext());
      assertThat(creatorsSlice.getContent()).hasSize(3);
      assertThat(creatorsSlice.getContent().get(0).getSubscriberAmount()).isEqualTo(3);
      assertThat(creatorsSlice.getContent().get(1).getSubscriberAmount()).isEqualTo(2);
      assertThat(creatorsSlice.getContent().get(2).getSubscriberAmount()).isEqualTo(1);
    }

    @Test
    @DisplayName("특정 카테고리에 대해 크리에이터 정보와 구독자 수를 조회할 수 있다.")
    void retrieveCreatorsByCategory() {
      Slice<CreatorAndSubscriptionCountMapper> creatorsSlice = creatorRepository.findAllCreatorsByCategoryId(
          developCategory.getId(), PageRequest.of(0, 10));

      assertEquals(false, creatorsSlice.hasNext());
      assertThat(creatorsSlice.getContent()).hasSize(2);
      assertThat(creatorsSlice.getContent().get(0).getSubscriberAmount()).isEqualTo(3);
      assertThat(creatorsSlice.getContent().get(1).getSubscriberAmount()).isEqualTo(2);
    }

    @Test
    @DisplayName("모든 카테고리의 크리에이터에 대해 내가 구독했는지 여부를 조회할 수 있다.")
    void retrieveIsSubscribedForAllCategory() {
      Slice<SubscribeFlagMapper> flags = creatorRepository.findCreatorIdAndSubscribeFlagByMemberId(
          member1.getId(), PageRequest.of(0, 10));

      assertEquals(false, flags.hasNext());
      assertThat(flags.getContent()).hasSize(3);
      assertThat(flags.getContent().get(0).getIsSubscribed()).isEqualTo(true);
      assertThat(flags.getContent().get(1).getIsSubscribed()).isEqualTo(true);
      assertThat(flags.getContent().get(2).getIsSubscribed()).isEqualTo(false);
    }

    @Test
    @DisplayName("특정 카테고리의 크리에이터에 대해 내가 구독했는지 여부를 조회할 수 있다.")
    void retrieveIsSubscribedByCategory() {
      Slice<SubscribeFlagMapper> flags = creatorRepository.findCreatorIdAndSubscribeFlagByMemberIdAndCategoryId(
          member1.getId(), developCategory.getId(), PageRequest.of(0, 10));

      assertEquals(false, flags.hasNext());
      assertThat(flags.getContent()).hasSize(2);
      assertThat(flags.getContent().get(0).getIsSubscribed()).isEqualTo(true);
      assertThat(flags.getContent().get(1).getIsSubscribed()).isEqualTo(true);

      Slice<SubscribeFlagMapper> flags2 = creatorRepository.findCreatorIdAndSubscribeFlagByMemberIdAndCategoryId(
          member1.getId(), beautyCategory.getId(), PageRequest.of(0, 10));

      assertEquals(false, flags2.hasNext());
      assertThat(flags2.getContent()).hasSize(1);
      assertThat(flags2.getContent().get(0).getIsSubscribed()).isEqualTo(false);
    }


  }
}
