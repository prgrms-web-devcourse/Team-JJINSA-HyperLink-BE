package com.hyperlink.server.content.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.dto.SearchResponse;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.memberHistory.application.MemberHistoryService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest
@Transactional
@DisplayName("ContentService 통합 테스트")
public class ContentServiceIntegrationTest {

  @Autowired
  ContentService contentService;
  @Autowired
  ContentRepository contentRepository;
  @Autowired
  CreatorRepository creatorRepository;
  @Autowired
  CategoryRepository categoryRepository;
  @Autowired
  MemberRepository memberRepository;
  @MockBean
  MemberHistoryService memberHistoryService;

  Creator creator;
  Category category;

  @BeforeAll
  void setUp() {
    category = new Category("개발");
    categoryRepository.save(category);
    creator = new Creator("슈카", "profile", "description", category);
    creatorRepository.save(creator);
  }

  @AfterAll
  void tearDown() {
    creatorRepository.deleteById(creator.getId());
    categoryRepository.deleteById(category.getId());
  }

  @Nested
  @DisplayName("조회수 갯수 조회 메서드는")
  class View {

    @Test
    @DisplayName("성공하면 특정 컨텐츠의 조회수를 리턴한다.")
    void success() {
      Content content = new Content("title", "contentImgUrl", "link", creator, category);
      contentRepository.save(content);
      int inquiryCountBeforeAdd = content.getViewCount();

      contentService.addView(null, content.getId());

      int findInquiry = contentService.getViewCount(content.getId());

      assertThat(findInquiry).isEqualTo(inquiryCountBeforeAdd + 1);
    }

    @Test
    @DisplayName("없는 컨텐츠를 요청했을 때 ContentNotFoundException가 발생한다.")
    void fail() {
      assertThrows(ContentNotFoundException.class, () -> {
        contentService.getViewCount(0L);
      });
    }
  }

  @Nested
  @DisplayName("조회수 추가 메서드는")
  class AddViewAndGetCount {

    @Nested
    @DisplayName("비회원 사용자가 요청했을 때")
    class MemberIdIsNull {

      @Test
      @DisplayName("조회수가 +1 처리되고, 히스토리 내역에 추가된 데이터가 없다")
      void addViewPlusOneAndNothingChangeHistory() {
        Long memberId = null;
        Content content = new Content("title", "contentImgUrl", "link", creator, category);
        content = contentRepository.save(content);
        int beforeViewCount = content.getViewCount();

        contentService.addView(memberId, content.getId());

        Content findContent = contentRepository.findById(content.getId())
            .orElseThrow(ContentNotFoundException::new);
        assertThat(findContent.getViewCount()).isEqualTo(beforeViewCount + 1);
      }
    }

    @Nested
    @DisplayName("로그인된 사용자가 요청했을 때")
    class RequestMember {

      @Test
      @DisplayName("사용자의 히스토리 내역에 해당 콘텐츠 데이터가 추가된다")
      void addMemberHistory() {
        Member member = new Member("email", "nickname", "career", "3", "profileImgUrl");
        memberRepository.save(member);

        Content content = new Content("title", "contentImgUrl", "link", creator, category);
        content = contentRepository.save(content);

        contentService.addView(member.getId(), content.getId());

        verify(memberHistoryService, times(1)).insertMemberHistory(any(), any());
      }
    }

    @TestMethodOrder(OrderAnnotation.class)
    @TestInstance(Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("다수의 사용자가 동시에")
    class ConcurrencyTest {

      static Long contentId;
      final int memberCount = 3;
      int beforeViewCount;
      final CountDownLatch countDownLatch = new CountDownLatch(memberCount);

      @BeforeAll
      void contentSetUpConcurrencyTest() {
        Content content = new Content("title", "contentImgUrl", "link", creator, category);
        content = contentRepository.save(content);
        contentId = content.getId();
        beforeViewCount = content.getViewCount();
      }

      @Order(1)
      @Rollback(value = false)
      @Test
      @DisplayName("조회수 추가를 요청했을 때")
      void manyRequestView() throws InterruptedException {
        List<Thread> workers = Stream.generate(() -> new Thread(new Worker(countDownLatch, contentId)))
            .limit(memberCount)
            .toList();
        workers.forEach(Thread::start);
        countDownLatch.await();
      }

      @Order(2)
      @Test
      @DisplayName("조회수가 요청 수만큼 증가한다.")
      void addView() {
        Content findContent = contentRepository.findById(contentId)
            .orElseThrow(ContentNotFoundException::new);
        assertThat(findContent.getViewCount()).isEqualTo(beforeViewCount + memberCount);
      }

      @Order(3)
      @Rollback(value = false)
      @Test
      void tearDown() {
        contentRepository.deleteById(contentId);
      }
    }


    private class Worker implements Runnable {

      private CountDownLatch countDownLatch;
      private Long contentId;

      public Worker(CountDownLatch countDownLatch, Long contentId) {
        this.countDownLatch = countDownLatch;
        this.contentId = contentId;
      }

      @Override
      public void run() {
        contentService.addView(null, contentId);
        countDownLatch.countDown();
      }
    }

  }

  @TestInstance(Lifecycle.PER_CLASS)
  @Nested
  @DisplayName("검색 메서드는")
  class Search {

    @BeforeEach
    void setUp() {
      List<Content> contents = new ArrayList<>();
      contents.add(new Content("개발", "ImgUrl", "link", creator, category));
      contents.add(new Content("개발짱", "ImgUrl", "link", creator, category));
      contents.add(new Content("짱개발짱", "ImgUrl", "link", creator, category));
      contents.add(new Content("개발자의 성장하는 삶", "ImgUrl", "link", creator, category));
      contents.add(new Content("키가 쑥쑥 성장판", "ImgUrl", "link", creator, category));
      contents.add(new Content("짱코딩짱", "ImgUrl", "link", creator, category));

      contentRepository.saveAll(contents);
    }

    Stream<Arguments> createResult() {
      return Stream.of(
          Arguments.of("개발 성장 코딩", 6),
          Arguments.of("개발", 4),
          Arguments.of("성장 개발", 5),
          Arguments.of("코딩", 1)
      );
    }

    @ParameterizedTest
    @MethodSource("createResult")
    @DisplayName("띄어쓰기를 기준으로 모든 키워드를 or 조건으로 containing 검색한 결과를 리턴한다.")
    void everyKeywordSearchContaining(String keyword, int resultCount) {
      Long memberId = 1L;
      final int page = 0;
      final int size = 10;
      Pageable pageable = PageRequest.of(page, size);

      SearchResponse searchResponse = contentService.search(memberId, keyword, pageable);

      assertThat(searchResponse.resultCount()).isEqualTo(resultCount);
    }
  }
}
