package com.hyperlink.server.memberHistory.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.dto.ContentResponse;
import com.hyperlink.server.domain.content.dto.GetContentsCommonResponse;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.memberHistory.application.MemberHistoryService;
import com.hyperlink.server.domain.memberHistory.domain.entity.MemberHistory;
import com.hyperlink.server.domain.memberHistory.domain.MemberHistoryRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    List<MemberHistory> findMemberHistory = memberHistoryRepository.findAllByMemberId(
        member.getId());
    assertThat(findMemberHistory.get(0).getMember()).isEqualTo(member);
    assertThat(findMemberHistory.get(0).getContent()).isEqualTo(content);
  }

  @Nested
  @DisplayName("히스토리 전체 조회 메소드는")
  class GetAllHistoryTest {

    Member member;
    List<Content> contents = new ArrayList<>();

    @BeforeEach
    void setUp() {
      member = new Member("email", "nickname", Career.DEVELOP, CareerYear.THREE, "profileImgUrl");
      memberRepository.save(member);

      LocalDateTime now = LocalDateTime.now();
      contents.add(new Content("개발", "ImgUrl", "link", creator, category));
      contents.add(new Content("개발짱", "ImgUrl", "link", creator, category));
      contents.add(new Content("짱개발짱", "ImgUrl", "link", creator, category));
      contents.add(new Content("개발자의 성장하는 삶", "ImgUrl", "link", creator, category));
      contents.add(new Content("키가 쑥쑥 성장판", "ImgUrl", "link", creator, category));
      contents.add(new Content("짱코딩짱", "ImgUrl", "link", creator, category));

      contentRepository.saveAll(contents);

      for (Content content : contents) {
        MemberHistory memberHistory = new MemberHistory(member, content);
        memberHistoryRepository.save(memberHistory);
      }
    }

    @Test
    @DisplayName("전체 히스토리 리스트를 요청에 맞는 size로 응답한다")
    void getAllHistoryTest() {
      Long memberId = member.getId();
      final int page = 0;
      final int size = 2;
      Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

      GetContentsCommonResponse findAllHistory = memberHistoryService.getAllHistory(memberId,
          pageable);
      List<ContentResponse> contentResponses = findAllHistory.contents();

      assertThat(findAllHistory.hasNext()).isTrue();
      assertThat(contentResponses).hasSize(2);
    }
  }
}
