package com.hyperlink.server.content.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.hyperlink.server.domain.attentionCategory.domain.AttentionCategoryRepository;
import com.hyperlink.server.domain.attentionCategory.domain.entity.AttentionCategory;
import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.exception.CategoryNotFoundException;
import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.dto.ContentEnrollResponse;
import com.hyperlink.server.domain.content.dto.ContentResponse;
import com.hyperlink.server.domain.content.dto.GetContentsCommonResponse;
import com.hyperlink.server.domain.content.dto.SearchResponse;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import com.hyperlink.server.domain.creator.domain.CreatorRepository;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.creator.exception.CreatorNotFoundException;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.memberHistory.application.MemberHistoryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
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
  @Autowired
  AttentionCategoryRepository attentionCategoryRepository;
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

      contentService.addView(Optional.empty(), content.getId());

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
        Optional<Long> memberId = Optional.empty();
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
        Member member = new Member("email", "nickname", Career.DEVELOP, CareerYear.MORE_THAN_TEN,
            "profileImgUrl");
        memberRepository.save(member);

        Content content = new Content("title", "contentImgUrl", "link", creator, category);
        content = contentRepository.save(content);

        contentService.addView(Optional.of(member.getId()), content.getId());

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
        List<Thread> workers = Stream.generate(
                () -> new Thread(new Worker(countDownLatch, contentId)))
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
        contentService.addView(Optional.empty(), contentId);
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

  @TestInstance(Lifecycle.PER_CLASS)
  @Nested
  @DisplayName("컨텐츠 추가 메서드는")
  class InsertContent {

    @Test
    @DisplayName("성공하면 컨텐츠를 추가한다.")
    public void insertContentSuccess() {
      ContentEnrollResponse contentEnrollResponse = new ContentEnrollResponse("게시글", "link",
          "contentImgLink", category.getName(),
          creator.getName());

      Long savedContentId = contentService.insertContent(contentEnrollResponse);

      assertThat(contentRepository.findById(savedContentId)).isPresent();
    }

    @Nested
    @DisplayName("[실패]")
    class Failure {

      @Test
      @DisplayName("이미 저장된 컨텐츠를 다시 저장하려고하면 저장하지 않고 -1을 반환한다.")
      void insertFailIfExists() {
        ContentEnrollResponse contentEnrollResponse = new ContentEnrollResponse("게시글", "link",
            "contentImgLink", category.getName(),
            creator.getName());
        ContentEnrollResponse sameContentEnrollResponse = new ContentEnrollResponse("게시글", "link",
            "contentImgLink", category.getName(),
            creator.getName());

        contentService.insertContent(contentEnrollResponse);
        Long insertResult = contentService.insertContent(sameContentEnrollResponse);

        assertEquals(-1L, (long) insertResult);
      }

      @Test
      @DisplayName("크리에이터의 이름이 잘못 입력되면 CreatorNotFoundException 을 발생한다.")
      void creatorNotFoundExceptionTest() {
        ContentEnrollResponse contentEnrollResponse = new ContentEnrollResponse("게시글", "link",
            "contentImgLink", category.getName(),
            "잘못된 크리에이터 명");

        assertThrows(CreatorNotFoundException.class,
            () -> contentService.insertContent(contentEnrollResponse));
      }

      @Test
      @DisplayName("카테고리의 이름이 잘못 입력되면 CategoryNotFoundException 을 발생한다.")
      void categoryNotFoundExceptionTest() {
        ContentEnrollResponse contentEnrollResponse = new ContentEnrollResponse("게시글", "link",
            "contentImgLink", "잘못된 카테고리명",
            creator.getName());

        assertThrows(CategoryNotFoundException.class,
            () -> contentService.insertContent(contentEnrollResponse));
      }
    }
  }

  @Nested
  @DisplayName("[Admin] 컨텐츠 활성화 메서드는")
  class ActivateContent {

    @TestInstance(Lifecycle.PER_CLASS)
    @TestMethodOrder(OrderAnnotation.class)
    @Rollback(value = false)
    @Nested
    @DisplayName("[성공] 활성화 버튼을 누르면")
    class Success {

      Content savedContent;

      @BeforeAll
      void setUp() {
        savedContent = contentRepository.save(
            new Content("개발글은 이렇게!!!!!", "contentImgUrl", "link", creator, category));
      }

      @Order(1)
      @Test
      @DisplayName("컨텐츠의 is_viewable을 true로 변경하고")
      void makeIsViewablePropertyTrue() {
        contentService.activateContent(savedContent.getId());
        Content content = contentRepository.findById(savedContent.getId()).get();

        assertTrue(content.isViewable());
      }

      @Order(2)
      @Test
      @DisplayName("true를 갖는다.")
      void checkIsViewablePropertyTrue() {
        Content content = contentRepository.findById(savedContent.getId()).get();

        assertTrue(content.isViewable());
      }


      @AfterAll
      void tearDown() {
        contentRepository.deleteById(savedContent.getId());
      }

    }

    @Nested
    @DisplayName("[실패]")
    class Fail {
      @Test
      @DisplayName("유효하지 않은 content id에 대해서는 ContentNotFoundException 을 발생한다.")
      void throwContentNotFoundExceptionWhenInvalidContentId() {
        Long invalidContentId = -1L;

        assertThrows(ContentNotFoundException.class,
            () -> contentService.activateContent(invalidContentId));

      }
    }
  }

  @Nested
  @DisplayName("컨텐츠 조회 메서드는")
  class Retrieve {

    Content content1;
    Content content2;
    Content content3;
    Content content4;
    Member member;

    @BeforeEach
    void setUp() {
      Creator creator2 = new Creator("코딩팩토리", "profileUrl", "description", category);
      creatorRepository.save(creator2);
      Category category2 = new Category("패션");
      categoryRepository.save(category2);

      content1 = contentRepository.save(
          new Content("제목1", "contentImgUrl1", "link1", creator, category));
      content2 = contentRepository.save(
          new Content("제목2", "contentImgUrl2", "link2", creator, category));
      content3 = contentRepository.save(
          new Content("제목3", "contentImgUrl3", "link3", creator, category));
      content4 = contentRepository.save(
          new Content("제목4", "contentImgUrl4", "link4", creator2, category2));
      member = new Member("memberEmail", "nickname", Career.DEVELOP,
          CareerYear.LESS_THAN_ONE,
          "profileImgUrl");
      memberRepository.save(member);
      attentionCategoryRepository.save(new AttentionCategory(member, category));
    }

    @Nested
    @DisplayName("트렌드를")
    class TrendContent {

      @Nested
      @DisplayName("카테고리 별로")
      class ByCategory {
        @Test
        @DisplayName("최신순으로 조회할 수 있다.")
        void retrieveRecent() {
          GetContentsCommonResponse getContentsCommonResponse = contentService.retrieveTrendContents(
              null, "개발", "recent", PageRequest.of(0, 10));

          List<ContentResponse> contents = getContentsCommonResponse.contents();
          assertThat(contents).hasSize(3);
          assertThat(contents.get(0).createdAt()).isLessThanOrEqualTo(
              contents.get(contents.size() - 1).createdAt());
        }

        @Test
        @DisplayName("인기순으로 조회할 수 있다.")
        void retrievePopular() {
          contentService.addView(Optional.of(member.getId()), content3.getId());
          contentService.addView(Optional.of(member.getId()), content3.getId());

          GetContentsCommonResponse getContentsCommonResponse = contentService.retrieveTrendContents(
              null, "개발", "popular", PageRequest.of(0, 10));

          List<ContentResponse> contents = getContentsCommonResponse.contents();
          assertThat(contents).hasSize(3);
          assertThat(contents.get(0).title()).isEqualTo("제목3");
          assertThat(contents.get(1).title()).isEqualTo("제목1");
        }

        @Nested
        @DisplayName("[실패]")
        class Fail {
          @Test
          @DisplayName("카테고리가 잘못 입력되면 CategoryNotFoundException을 발생한다.")
          void throwCategoryNotFoundExceptionForInvalidCategory() {
            String categoryName = "invalidCategory";

            assertThrows(CategoryNotFoundException.class,
                () -> contentService.retrieveTrendContents(null, categoryName, "recent",
                    PageRequest.of(0, 10)));
          }

        }
      }

      @Nested
      @DisplayName("전체 카테고리 조회 시")
      class AllCategory {

        @Nested
        @DisplayName("로그인하지 않은 유저라면")
        class IsNotLogin {
          @Test
          @DisplayName("카테고리 전체에 대해 최신순으로 조회할 수 있다.")
          public void retrieveForAttentionCategoryByRecent() throws Exception {
            GetContentsCommonResponse getContentsCommonResponse = contentService.retrieveTrendAllCategoriesContents(
                null, "recent", PageRequest.of(0, 10));

            List<ContentResponse> contents = getContentsCommonResponse.contents();
            assertThat(contents).hasSize(4);
            assertThat(contents.get(0).createdAt()).isLessThanOrEqualTo(
                contents.get(contents.size() - 1).createdAt());
          }

          @Test
          @DisplayName("카테고리 전체에 대해 인기순으로 조회할 수 있다.")
          public void retrieveForAttentionCategoryByPopular() throws Exception {
            contentService.addView(Optional.of(member.getId()), content4.getId());
            contentService.addView(Optional.of(member.getId()), content4.getId());
            contentService.addView(Optional.of(member.getId()), content4.getId());

            contentService.addView(Optional.of(member.getId()), content3.getId());
            contentService.addView(Optional.of(member.getId()), content3.getId());

            GetContentsCommonResponse getContentsCommonResponse = contentService.retrieveTrendAllCategoriesContents(
                null, "popular", PageRequest.of(0, 10));

            List<ContentResponse> contents = getContentsCommonResponse.contents();
            assertThat(contents).hasSize(4);
            assertThat(contents.get(0).title()).isEqualTo("제목4");
            assertThat(contents.get(1).title()).isEqualTo("제목3");
          }
        }

        @Nested
        @DisplayName("로그인한 유저라면")
        class IsLogin {
          @Test
          @DisplayName("유저의 관심 카테고리 전체에 대해 최신순으로 조회할 수 있다.")
          public void retrieveForAttentionCategoryByRecent() throws Exception {
            GetContentsCommonResponse getContentsCommonResponse = contentService.retrieveTrendAllCategoriesContents(
                member.getId(), "recent", PageRequest.of(0, 10));

            List<ContentResponse> contents = getContentsCommonResponse.contents();
            assertThat(contents).hasSize(3);
            assertThat(contents.get(0).createdAt()).isLessThanOrEqualTo(
                contents.get(contents.size() - 1).createdAt());
          }

          @Test
          @DisplayName("유저의 관심 카테고리 전체에 대해 인기순으로 조회할 수 있다.")
          public void retrieveForAttentionCategoryByPopular() throws Exception {
            contentService.addView(Optional.of(member.getId()), content4.getId());
            contentService.addView(Optional.of(member.getId()), content4.getId());
            contentService.addView(Optional.of(member.getId()), content4.getId());

            contentService.addView(Optional.of(member.getId()), content3.getId());
            contentService.addView(Optional.of(member.getId()), content3.getId());

            GetContentsCommonResponse getContentsCommonResponse = contentService.retrieveTrendAllCategoriesContents(
                member.getId(), "popular", PageRequest.of(0, 10));

            List<ContentResponse> contents = getContentsCommonResponse.contents();
            assertThat(contents).hasSize(3);
            assertThat(contents.get(0).title()).isEqualTo("제목3");
            assertThat(contents.get(1).title()).isEqualTo("제목1");
          }
        }
      }


    }

    @Nested
    @DisplayName("크리에이터의 글을")
    class CreatorContent {
      @Test
      @DisplayName("최신순으로 조회할 수 있다.")
      void retrieveRecent() {
        GetContentsCommonResponse getContentsCommonResponse = contentService.retrieveCreatorContents(
            null, creator.getId(), "recent", PageRequest.of(0, 10));

        List<ContentResponse> contents = getContentsCommonResponse.contents();
        assertThat(contents).hasSize(3);
        assertThat(contents.get(0).createdAt()).isLessThanOrEqualTo(
            contents.get(contents.size() - 1).createdAt());
      }

      @Test
      @DisplayName("인기순으로 조회할 수 있다.")
      void retrievePopular() {
        Member member = memberRepository.save(new Member("email", "nickname", Career.DEVELOP,
            CareerYear.LESS_THAN_ONE, "profileImgUrl"));
        memberRepository.save(member);
        contentService.addView(Optional.of(member.getId()), content4.getId());
        contentService.addView(Optional.of(member.getId()), content4.getId());
        contentService.addView(Optional.of(member.getId()), content4.getId());

        contentService.addView(Optional.of(member.getId()), content3.getId());
        contentService.addView(Optional.of(member.getId()), content3.getId());

        GetContentsCommonResponse getContentsCommonResponse = contentService.retrieveCreatorContents(
            null, creator.getId(), "popular", PageRequest.of(0, 10));

        List<ContentResponse> contents = getContentsCommonResponse.contents();
        assertThat(contents).hasSize(3);
        assertThat(contents.get(0).title()).isEqualTo("제목3");
      }
    }
  }
}
