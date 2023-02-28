package com.hyperlink.server.creator.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.creator.application.CreatorService;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.creator.exception.CreatorNotFoundException;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.domain.memberCreator.domain.MemberCreatorRepository;
import com.hyperlink.server.domain.memberCreator.domain.entity.MemberCreator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DisplayName("CreatorService 통합 테스트")
public class CreatorServiceIntegrationTest {

  @Autowired
  CreatorService creatorService;
  @Autowired
  MemberCreatorRepository memberCreatorRepository;
  @Autowired
  MemberRepository memberRepository;
  @Autowired
  CreatorRepository creatorRepository;
  @Autowired
  CategoryRepository categoryRepository;

  @Nested
  @DisplayName("비추천 크리에이터 추가 메소드는")
  class NotRecommend {

    Member member;
    Creator creator;
    Category category;

    @BeforeEach
    void setUp() {
      member = new Member("email", "nickname", "career", "careerYear", "profileImgUrl");
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

      MemberCreator notRecommendCreator = creatorService.notRecommend(memberId, creatorId);

      List<MemberCreator> memberNotRecommendCreators = memberCreatorRepository.findByMemberId(memberId);

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

}
