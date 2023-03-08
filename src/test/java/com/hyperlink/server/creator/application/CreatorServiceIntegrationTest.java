package com.hyperlink.server.creator.application;

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
import com.hyperlink.server.domain.creator.exception.CreatorNotFoundException;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.domain.notRecommendCreator.domain.NotRecommendCreatorRepository;
import com.hyperlink.server.domain.notRecommendCreator.domain.entity.NotRecommendCreator;
import java.util.List;
import java.util.Optional;
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
