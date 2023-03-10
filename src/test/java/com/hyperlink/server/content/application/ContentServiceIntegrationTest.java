package com.hyperlink.server.content.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.hyperlink.server.domain.attentionCategory.domain.AttentionCategoryRepository;
import com.hyperlink.server.domain.attentionCategory.domain.entity.AttentionCategory;
import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.category.exception.CategoryNotFoundException;
import com.hyperlink.server.domain.common.ContentDtoFactoryService;
import com.hyperlink.server.domain.company.domain.CompanyRepository;
import com.hyperlink.server.domain.company.domain.entity.Company;
import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.content.domain.ContentRepository;
import com.hyperlink.server.domain.content.domain.entity.Content;
import com.hyperlink.server.domain.content.dto.ContentAdminResponses;
import com.hyperlink.server.domain.content.dto.ContentEnrollResponse;
import com.hyperlink.server.domain.content.dto.ContentResponse;
import com.hyperlink.server.domain.content.dto.ContentViewerRecommendationResponse;
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
import com.hyperlink.server.domain.memberContent.domain.MemberContentRepository;
import com.hyperlink.server.domain.memberContent.domain.entity.MemberContent;
import com.hyperlink.server.domain.memberContent.domain.entity.MemberContentActionType;
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
@DisplayName("ContentService ?????? ?????????")
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
  @Autowired
  MemberContentRepository memberContentRepository;
  @Autowired
  CompanyRepository companyRepository;
  @Autowired
  ContentDtoFactoryService contentDtoFactoryService;


  Creator creator;
  Category category;

  @BeforeAll
  void setUp() {
    category = new Category("??????10");
    categoryRepository.save(category);
    creator = new Creator("??????", "profile", "description", category);
    creatorRepository.save(creator);
  }

  @AfterAll
  void tearDown() {
    creatorRepository.deleteById(creator.getId());
    categoryRepository.deleteById(category.getId());
  }

  @Nested
  @DisplayName("????????? ?????? ?????? ????????????")
  class View {

    @Test
    @DisplayName("???????????? ?????? ???????????? ???????????? ????????????.")
    void success() {
      Content content = new Content("title", "contentImgUrl", "link", creator, category);
      contentRepository.save(content);
      int inquiryCountBeforeAdd = content.getViewCount();

      contentService.addView(null, content.getId(), false);

      int findInquiry = contentService.getViewCount(content.getId());

      assertThat(findInquiry).isEqualTo(inquiryCountBeforeAdd + 1);
    }

    @Test
    @DisplayName("?????? ???????????? ???????????? ??? ContentNotFoundException??? ????????????.")
    void fail() {
      assertThrows(ContentNotFoundException.class, () -> {
        contentService.getViewCount(0L);
      });
    }
  }

  @Nested
  @DisplayName("????????? ?????? ????????????")
  class AddViewAndGetCount {

    @Nested
    @DisplayName("????????? ???????????? ???????????? ???")
    class MemberIdIsNull {

      @Test
      @DisplayName("???????????? +1 ????????????, ???????????? ????????? ????????? ???????????? ??????")
      void addViewPlusOneAndNothingChangeHistory() {
        Long memberId = null;
        Content content = new Content("title", "contentImgUrl", "link", creator, category);
        content = contentRepository.save(content);
        int beforeViewCount = content.getViewCount();

        contentService.addView(memberId, content.getId(), false);

        Content findContent = contentRepository.findById(content.getId())
            .orElseThrow(ContentNotFoundException::new);
        assertThat(findContent.getViewCount()).isEqualTo(beforeViewCount + 1);
      }
    }

    @Nested
    @DisplayName("???????????? ???????????? ???????????? ???")
    class RequestMember {

      @Test
      @DisplayName("???????????? ???????????? ????????? ?????? ????????? ???????????? ????????????")
      void addMemberHistory() {
        Member member = new Member("email", "nickname", Career.DEVELOP, CareerYear.MORE_THAN_TEN,
            "profileImgUrl");
        memberRepository.save(member);

        Content content = new Content("title", "contentImgUrl", "link", creator, category);
        content = contentRepository.save(content);

        contentService.addView(member.getId(), content.getId(), false);

        verify(memberHistoryService, times(1)).insertMemberHistory(any(), any(), anyBoolean());
      }
    }

    @TestMethodOrder(OrderAnnotation.class)
    @TestInstance(Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("????????? ???????????? ?????????")
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
      @DisplayName("????????? ????????? ???????????? ???")
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
      @DisplayName("???????????? ?????? ????????? ????????????.")
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
        contentService.addView(null, contentId, false);
        countDownLatch.countDown();
      }
    }

  }

  @TestInstance(Lifecycle.PER_CLASS)
  @Nested
  @DisplayName("?????? ????????????")
  class Search {

    @BeforeEach
    void setUp() {
      List<Content> contents = new ArrayList<>();
      contents.add(new Content("??????", "ImgUrl", "link", creator, category));
      contents.add(new Content("?????????", "ImgUrl", "link", creator, category));
      contents.add(new Content("????????????", "ImgUrl", "link", creator, category));
      contents.add(new Content("???????????? ???????????? ???", "ImgUrl", "link", creator, category));
      contents.add(new Content("?????? ?????? ?????????", "ImgUrl", "link", creator, category));
      contents.add(new Content("????????????", "ImgUrl", "link", creator, category));

      contentRepository.saveAll(contents);
    }

    Stream<Arguments> createResult() {
      return Stream.of(
          Arguments.of("?????? ?????? ??????", 6),
          Arguments.of("??????", 4),
          Arguments.of("?????? ??????", 5),
          Arguments.of("??????", 1)
      );
    }

    @ParameterizedTest
    @MethodSource("createResult")
    @DisplayName("??????????????? ???????????? ?????? ???????????? or ???????????? containing ????????? ????????? ????????????.")
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
  @DisplayName("????????? ?????? ????????????")
  class InsertContent {

    @Test
    @DisplayName("???????????? ???????????? ????????????.")
    public void insertContentSuccess() {
      ContentEnrollResponse contentEnrollResponse = new ContentEnrollResponse("?????????", "link",
          "contentImgLink", category.getName(),
          creator.getName());

      Long savedContentId = contentService.insertContent(contentEnrollResponse);

      assertThat(contentRepository.findById(savedContentId)).isPresent();
    }

    @Nested
    @DisplayName("[??????]")
    class Failure {

      @Test
      @DisplayName("?????? ????????? ???????????? ?????? ????????????????????? ???????????? ?????? -1??? ????????????.")
      void insertFailIfExists() {
        ContentEnrollResponse contentEnrollResponse = new ContentEnrollResponse("?????????", "link",
            "contentImgLink", category.getName(),
            creator.getName());
        ContentEnrollResponse sameContentEnrollResponse = new ContentEnrollResponse("?????????", "link",
            "contentImgLink", category.getName(),
            creator.getName());

        contentService.insertContent(contentEnrollResponse);
        Long insertResult = contentService.insertContent(sameContentEnrollResponse);

        assertEquals(-1L, (long) insertResult);
      }

      @Test
      @DisplayName("?????????????????? ????????? ?????? ???????????? CreatorNotFoundException ??? ????????????.")
      void creatorNotFoundExceptionTest() {
        ContentEnrollResponse contentEnrollResponse = new ContentEnrollResponse("?????????", "link",
            "contentImgLink", category.getName(),
            "????????? ??????????????? ???");

        assertThrows(CreatorNotFoundException.class,
            () -> contentService.insertContent(contentEnrollResponse));
      }

      @Test
      @DisplayName("??????????????? ????????? ?????? ???????????? CategoryNotFoundException ??? ????????????.")
      void categoryNotFoundExceptionTest() {
        ContentEnrollResponse contentEnrollResponse = new ContentEnrollResponse("?????????", "link",
            "contentImgLink", "????????? ???????????????",
            creator.getName());

        assertThrows(CategoryNotFoundException.class,
            () -> contentService.insertContent(contentEnrollResponse));
      }
    }
  }

  @Nested
  @DisplayName("[Admin] ????????? ????????? ????????????")
  class ActivateContent {

    @TestInstance(Lifecycle.PER_CLASS)
    @TestMethodOrder(OrderAnnotation.class)
    @Rollback(value = false)
    @Nested
    @DisplayName("[??????] ????????? ????????? ?????????")
    class Success {

      Content savedContent;

      @BeforeAll
      void setUp() {
        savedContent = contentRepository.save(
            new Content("???????????? ?????????!!!!!", "contentImgUrl", "link", creator, category));
      }

      @Order(1)
      @Test
      @DisplayName("???????????? is_viewable??? true??? ????????????")
      void makeIsViewablePropertyTrue() {
        contentService.activateContent(savedContent.getId());
        Content content = contentRepository.findById(savedContent.getId()).get();

        assertTrue(content.isViewable());
      }

      @Order(2)
      @Test
      @DisplayName("true??? ?????????.")
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
    @DisplayName("[??????]")
    class Fail {

      @Test
      @DisplayName("???????????? ?????? content id??? ???????????? ContentNotFoundException ??? ????????????.")
      void throwContentNotFoundExceptionWhenInvalidContentId() {
        Long invalidContentId = -1L;

        assertThrows(ContentNotFoundException.class,
            () -> contentService.activateContent(invalidContentId));

      }
    }
  }

  @Nested
  @DisplayName("????????? ?????? ????????????")
  class Retrieve {

    Content content1;
    Content content2;
    Content content3;
    Content content4;
    Member member;

    @BeforeEach
    void setUp() throws InterruptedException {
      Creator creator2 = new Creator("???????????????", "profileUrl", "description", category);
      creatorRepository.save(creator2);
      Category category2 = new Category("??????");
      categoryRepository.save(category2);

      content1 = contentRepository.save(
          new Content("??????1", "contentImgUrl1", "link1", creator, category));
      Thread.sleep(1000);
      content2 = contentRepository.save(
          new Content("??????2", "contentImgUrl2", "link2", creator, category));
      Thread.sleep(1000);
      content3 = contentRepository.save(
          new Content("??????3", "contentImgUrl3", "link3", creator, category));
      Thread.sleep(1000);
      content4 = contentRepository.save(
          new Content("??????4", "contentImgUrl4", "link4", creator2, category2));

      contentService.activateContent(content1.getId());
      contentService.activateContent(content2.getId());
      contentService.activateContent(content3.getId());
      contentService.activateContent(content4.getId());

      member = new Member("memberEmail", "nickname", Career.DEVELOP,
          CareerYear.LESS_THAN_ONE,
          "profileImgUrl");
      memberRepository.save(member);
      attentionCategoryRepository.save(new AttentionCategory(member, category));
    }

    @Nested
    @DisplayName("????????????")
    class TrendContent {

      @Nested
      @DisplayName("???????????? ??????")
      class ByCategory {

        @Test
        @DisplayName("??????????????? ????????? ??? ??????.")
        void retrieveRecent() {
          GetContentsCommonResponse getContentsCommonResponse = contentService.retrieveTrendContents(
              null, "??????10", "recent", PageRequest.of(0, 10));

          List<ContentResponse> contents = getContentsCommonResponse.contents();
          assertThat(contents).hasSize(3);
          assertThat(contents.get(0).createdAt()).isGreaterThanOrEqualTo(
              contents.get(contents.size() - 1).createdAt());
        }

        @Test
        @DisplayName("??????????????? ????????? ??? ??????.")
        void retrievePopular() {
          contentService.addView(member.getId(), content3.getId(), false);
          contentService.addView(member.getId(), content3.getId(), false);

          GetContentsCommonResponse getContentsCommonResponse = contentService.retrieveTrendContents(
              null, "??????10", "popular", PageRequest.of(0, 10));

          List<ContentResponse> contents = getContentsCommonResponse.contents();
          assertThat(contents).hasSize(3);
          assertThat(contents.get(0).title()).isEqualTo("??????3");
          assertThat(contents.get(1).title()).isEqualTo("??????1");
        }

        @Nested
        @DisplayName("[??????]")
        class Fail {

          @Test
          @DisplayName("??????????????? ?????? ???????????? CategoryNotFoundException??? ????????????.")
          void throwCategoryNotFoundExceptionForInvalidCategory() {
            String categoryName = "invalidCategory";

            assertThrows(CategoryNotFoundException.class,
                () -> contentService.retrieveTrendContents(null, categoryName, "recent",
                    PageRequest.of(0, 10)));
          }

        }
      }

      @Nested
      @DisplayName("?????? ???????????? ?????? ???")
      class AllCategory {

        @Nested
        @DisplayName("??????????????? ?????? ????????????")
        class IsNotLogin {

          @Test
          @DisplayName("???????????? ????????? ?????? ??????????????? ????????? ??? ??????.")
          public void retrieveForAttentionCategoryByRecent() throws Exception {
            GetContentsCommonResponse getContentsCommonResponse = contentService.retrieveTrendAllCategoriesContents(
                null, "recent", PageRequest.of(0, 10));

            List<ContentResponse> contents = getContentsCommonResponse.contents();
            assertThat(contents).hasSize(4);
            assertThat(contents.get(0).createdAt()).isGreaterThanOrEqualTo(
                contents.get(contents.size() - 1).createdAt());
          }

          @Test
          @DisplayName("???????????? ????????? ?????? ??????????????? ????????? ??? ??????.")
          public void retrieveForAttentionCategoryByPopular() throws Exception {
            contentService.addView(member.getId(), content4.getId(), false);
            contentService.addView(member.getId(), content4.getId(), false);
            contentService.addView(member.getId(), content4.getId(), false);

            contentService.addView(member.getId(), content3.getId(), false);
            contentService.addView(member.getId(), content3.getId(), false);

            GetContentsCommonResponse getContentsCommonResponse = contentService.retrieveTrendAllCategoriesContents(
                null, "popular", PageRequest.of(0, 10));

            List<ContentResponse> contents = getContentsCommonResponse.contents();
            assertThat(contents).hasSize(4);
            assertThat(contents.get(0).title()).isEqualTo("??????4");
            assertThat(contents.get(1).title()).isEqualTo("??????3");
          }
        }

        @Nested
        @DisplayName("???????????? ????????????")
        class IsLogin {

          @Test
          @DisplayName("????????? ?????? ???????????? ????????? ?????? ??????????????? ????????? ??? ??????.")
          public void retrieveForAttentionCategoryByRecent() throws Exception {
            GetContentsCommonResponse getContentsCommonResponse = contentService.retrieveTrendAllCategoriesContents(
                member.getId(), "recent", PageRequest.of(0, 10));

            List<ContentResponse> contents = getContentsCommonResponse.contents();
            assertThat(contents).hasSize(3);
            assertThat(contents.get(0).createdAt()).isGreaterThanOrEqualTo(
                contents.get(contents.size() - 1).createdAt());
          }

          @Test
          @DisplayName("????????? ?????? ???????????? ????????? ?????? ??????????????? ????????? ??? ??????.")
          public void retrieveForAttentionCategoryByPopular() throws Exception {
            contentService.addView(member.getId(), content4.getId(), false);
            contentService.addView(member.getId(), content4.getId(), false);
            contentService.addView(member.getId(), content4.getId(), false);

            contentService.addView(member.getId(), content3.getId(), false);
            contentService.addView(member.getId(), content3.getId(), false);

            GetContentsCommonResponse getContentsCommonResponse = contentService.retrieveTrendAllCategoriesContents(
                member.getId(), "popular", PageRequest.of(0, 10));

            List<ContentResponse> contents = getContentsCommonResponse.contents();
            assertThat(contents).hasSize(3);
            assertThat(contents.get(0).title()).isEqualTo("??????3");
            assertThat(contents.get(1).title()).isEqualTo("??????1");
          }
        }
      }


    }

    @Nested
    @DisplayName("?????????????????? ??????")
    class CreatorContent {

      @Test
      @DisplayName("??????????????? ????????? ??? ??????.")
      void retrieveRecent() {
        GetContentsCommonResponse getContentsCommonResponse = contentService.retrieveCreatorContents(
            null, creator.getId(), "recent", PageRequest.of(0, 10));

        List<ContentResponse> contents = getContentsCommonResponse.contents();
        assertThat(contents).hasSize(3);
        assertThat(contents.get(0).createdAt()).isGreaterThanOrEqualTo(
            contents.get(contents.size() - 1).createdAt());
      }

      @Test
      @DisplayName("??????????????? ????????? ??? ??????.")
      void retrievePopular() {
        Member member = memberRepository.save(new Member("email", "nickname", Career.DEVELOP,
            CareerYear.LESS_THAN_ONE, "profileImgUrl"));
        memberRepository.save(member);
        contentService.addView(member.getId(), content4.getId(), false);
        contentService.addView(member.getId(), content4.getId(), false);
        contentService.addView(member.getId(), content4.getId(), false);

        contentService.addView(member.getId(), content3.getId(), false);
        contentService.addView(member.getId(), content3.getId(), false);

        GetContentsCommonResponse getContentsCommonResponse = contentService.retrieveCreatorContents(
            null, creator.getId(), "popular", PageRequest.of(0, 10));

        List<ContentResponse> contents = getContentsCommonResponse.contents();
        assertThat(contents).hasSize(3);
        assertThat(contents.get(0).title()).isEqualTo("??????3");
      }
    }
  }

  @Nested
  @DisplayName("????????? ???????????? ?????? ?????? ????????????")
  class ContentViewerRecommend {

    @Nested
    @DisplayName("????????? ?????? ?????? ????????? ????????? 3??? ????????? ???????????? ?????? ?????? ?????? ????????? ????????????.")
    class RecommendCompany {

      Content content1;
      Member member1;
      Member member2;
      Member member3;
      Category category;

      @BeforeEach
      void setUp() throws InterruptedException {
        category = new Category("develop");
        categoryRepository.save(category);

        content1 = contentRepository.save(
            new Content("??????1", "contentImgUrl1", "link1", creator, category));

        member1 = new Member("member1Email", "nickname", Career.DEVELOP,
            CareerYear.LESS_THAN_ONE,
            "profileImgUrl");
        memberRepository.save(member1);

        member2 = new Member("member2Email", "nickname", Career.DEVELOP,
            CareerYear.LESS_THAN_ONE,
            "profileImgUrl");
        memberRepository.save(member2);

        member3 = new Member("member3Email", "nickname", Career.DEVELOP,
            CareerYear.LESS_THAN_ONE,
            "profileImgUrl");
        memberRepository.save(member3);

        memberContentRepository.save(
            new MemberContent(member1.getId(), content1, MemberContentActionType.LIKE));
        memberContentRepository.save(
            new MemberContent(member2.getId(), content1, MemberContentActionType.LIKE));
        memberContentRepository.save(
            new MemberContent(member3.getId(), content1, MemberContentActionType.LIKE));

        Company kakao = companyRepository.save(
            new Company("kakaoCorps.com", "?????????"));
        kakao.changeIsUsingRecommend(true);

        member1.changeCompany(kakao);
        member2.changeCompany(kakao);
        member3.changeCompany(kakao);
      }

      @Test
      @DisplayName("[??????]")
      void recommendCompanySuccessTest() {
        List<ContentViewerRecommendationResponse> contentViewerRecommendationResponse = contentDtoFactoryService.getContentViewerRecommendationResponse(
            category.getName(),
            content1.getId());

        assertThat(contentViewerRecommendationResponse).hasSize(1);
        assertThat(contentViewerRecommendationResponse.get(0).bannerName()).isEqualTo("?????????");
      }
    }


  }

  @TestInstance(Lifecycle.PER_CLASS)
  @TestMethodOrder(OrderAnnotation.class)
  @Nested
  @Rollback(value = false)
  @DisplayName("[????????? ?????????] ??????????????? ??????")
  class RetrieveInactivatedContent {

    Content content1;
    Content content2;
    Content content3;
    Content content4;
    Creator creator2;
    Category category2;
    Member member;
    AttentionCategory savedAttentionCategory;

    @Order(1)
    @Test
    void setUp() throws InterruptedException {
      creator2 = new Creator("???????????????", "profileUrl", "description", category);
      creatorRepository.save(creator2);
      category2 = new Category("??????");
      categoryRepository.save(category2);

      content1 = contentRepository.save(
          new Content("??????1", "contentImgUrl1", "link1", creator, category));
      Thread.sleep(1000);
      content2 = contentRepository.save(
          new Content("??????2", "contentImgUrl2", "link2", creator, category));
      Thread.sleep(1000);
      content3 = contentRepository.save(
          new Content("??????3", "contentImgUrl3", "link3", creator, category));
      Thread.sleep(1000);
      content4 = contentRepository.save(
          new Content("??????4", "contentImgUrl4", "link4", creator2, category2));

      member = new Member("memberEmail", "nickname", Career.DEVELOP,
          CareerYear.LESS_THAN_ONE,
          "profileImgUrl");
      memberRepository.save(member);
      savedAttentionCategory = attentionCategoryRepository.save(
          new AttentionCategory(member, category));
    }

    @Order(2)
    @Test
    @DisplayName("??????????????? ????????? ??? ??????.")
    void retrieveInactivatedContent() {
      Content newContent2 = contentRepository.findById(content2.getId()).get();
      contentService.activateContent(newContent2.getId());

      ContentAdminResponses contentAdminResponses = contentService.retrieveInactivatedContents(
          PageRequest.of(0, 10));

      assertThat(contentAdminResponses.contents()).hasSize(3);
      assertThat(contentAdminResponses.contents().get(0).title()).isEqualTo("??????4");
      assertThat(contentAdminResponses.contents().get(2).title()).isEqualTo("??????1");
    }

    @Order(3)
    @Test
    @DisplayName("????????? ??????")
    void tearDown() {
      attentionCategoryRepository.deleteById(savedAttentionCategory.getId());
      contentRepository.deleteById(content1.getId());
      contentRepository.deleteById(content2.getId());
      contentRepository.deleteById(content3.getId());
      contentRepository.deleteById(content4.getId());
      creatorRepository.deleteById(creator2.getId());
      memberRepository.deleteById(member.getId());
      categoryRepository.deleteById(category2.getId());
    }
  }

  @Nested
  @DisplayName("[????????? ?????????] ????????? ?????? ????????????")
  class DeleteContentsByAdmin {

    Content content;

    @BeforeEach
    void setUp() {
      content = new Content("??????1", "contentImgUrl", "link1", creator, category);
      contentRepository.save(content);
    }

    @Nested
    @DisplayName("[??????]")
    class Success {

      @Test
      @DisplayName("???????????? ???????????? ?????? ???????????? ????????????.")
      void deleteContentsById() {
        contentService.deleteContentsById(content.getId());

        assertThat(contentRepository.findById(content.getId()).isEmpty());
      }
    }

    @Nested
    @DisplayName("[??????]")
    class Fail {

      @Test
      @DisplayName("???????????? ???????????? ?????? ???????????? ????????????.")
      void deleteContentsById() {
        Long invalidContentId = -1L;

        assertThrows(ContentNotFoundException.class,
            () -> contentService.deleteContentsById(invalidContentId));
      }
    }
  }
}
