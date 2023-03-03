package com.hyperlink.server.memberHistory.application;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.hyperlink.server.domain.memberHistory.application.MemberHistoryService;
import com.hyperlink.server.domain.memberHistory.domain.entity.MemberHistory;
import com.hyperlink.server.domain.memberHistory.domain.entity.MemberHistoryRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DisplayName("MemberHistory 통합 테스트")
class MemberHistoryIntegrationTest {

  @Autowired
  MemberHistoryService memberHistoryService;
  @Autowired
  MemberHistoryRepository memberHistoryRepository;
  @Autowired
  CreatorRepository creatorRepository;
  @Autowired
  CategoryRepository categoryRepository;
  @Autowired
  MemberRepository memberRepository;
  @Autowired
  ContentRepository contentRepository;

  Creator creator;
  Category category;

  @BeforeEach
  void setUp() {
    category = new Category("개발");
    categoryRepository.save(category);
    creator = new Creator("슈카", "profile", "description", category);
    creatorRepository.save(creator);
  }

  @Test
  @DisplayName("히스토리 추가 메소드는 DB에 memberId, contentId가 저장된다")
  void insertMemberHistoryTest() {
    Member member = new Member("email", "nickname", Career.DEVELOP, CareerYear.FOUR,
        "profileImgUrl");
    memberRepository.save(member);

    Content content = new Content("title", "contentImgUrl", "link", creator, category);
    content = contentRepository.save(content);

    memberHistoryService.insertMemberHistory(member.getId(), content.getId());

    List<MemberHistory> findMemberHistory = memberHistoryRepository.findByMemberId(member.getId());
    assertThat(findMemberHistory.get(0).getMember()).isEqualTo(member);
    assertThat(findMemberHistory.get(0).getContent()).isEqualTo(content);
  }

}
