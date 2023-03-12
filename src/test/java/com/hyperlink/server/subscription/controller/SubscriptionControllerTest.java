package com.hyperlink.server.subscription.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.AuthSetupForMock;
import com.hyperlink.server.domain.content.dto.ContentResponse;
import com.hyperlink.server.domain.content.dto.GetContentsCommonResponse;
import com.hyperlink.server.domain.content.dto.ContentViewerRecommendationResponse;
import com.hyperlink.server.domain.creator.exception.CreatorNotFoundException;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.domain.subscription.application.SubscriptionService;
import com.hyperlink.server.domain.subscription.controller.SubscriptionController;
import com.hyperlink.server.domain.subscription.dto.SubscribeResponse;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingServletRequestParameterException;

@WebMvcTest(SubscriptionController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public class SubscriptionControllerTest extends AuthSetupForMock {

  @MockBean
  SubscriptionService subscriptionService;

  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;

  @Nested
  @DisplayName("크리에이터 구독 추가/삭제 API는")
  class SubscribeCreatorTest {

    @Nested
    @DisplayName("[성공]")
    class Success {

      @Nested
      @DisplayName("로그인 한")
      class IfLogin {

        @BeforeEach
        void setUp() {
          authSetup();
        }

        @Test
        @DisplayName("사용자가 크리에이터를 이미 구독한 상황이었다면 구독 취소하고 200 OK와 구독 상태를 반환한다.")
        void unsubscribeIfSubscribed() throws Exception {
          Long loginMemberId = 1L;
          Long creatorId = 1L;

          SubscribeResponse subscribeResponse = new SubscribeResponse(false);
          given(subscriptionService.subscribeOrUnsubscribeCreator(loginMemberId, creatorId))
              .willReturn(subscribeResponse);

          mockMvc.perform(
                  post("/creators/" + creatorId + "/subscribe")
                      .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                      .characterEncoding("UTF-8"))
              .andExpect(status().isOk())
              .andDo(print())
              .andDo(document("SubscriptionControllerTest/subscribeOrUnsubscribeCreator",
                  preprocessRequest(prettyPrint()),
                  preprocessResponse(prettyPrint()),
                  requestHeaders(
                      headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")
                  ),
                  responseFields(
                      fieldWithPath("isSubscribed").type(JsonFieldType.BOOLEAN)
                          .description("구독 여부 상태")
                  )
              ));
        }

        @Test
        @DisplayName("사용자가 크리에이터를 구독하지 않은 상황이었다면 구독하고 200 OK와 구독 상태를 반환한다.")
        void subscribeIfUnsubscribed() throws Exception {
          Long loginMemberId = 1L;
          Long creatorId = 1L;

          SubscribeResponse subscribeResponse = new SubscribeResponse(true);
          when(subscriptionService.subscribeOrUnsubscribeCreator(loginMemberId, creatorId))
              .thenReturn(subscribeResponse);

          mockMvc.perform(post("/creators/" + creatorId + "/subscribe")
                  .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                  .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andDo(print());
        }
      }
    }


    @Nested
    @DisplayName("[실패]")
    class Fail {

      @Nested
      @DisplayName("잘못된")
      class IfLogin {

        @BeforeEach
        void setUp() {
          authSetup();
        }

        @Test
        @DisplayName("사용자가 크리에이터를 크리에이터에 대해 구독버튼을 누르면 404 NOT FOUND 를  반환한다.")
        void throwsMemberNotFoundException() throws Exception {
          Long wrongMemberId = 1L;
          Long creatorId = 1L;

          when(subscriptionService.subscribeOrUnsubscribeCreator(wrongMemberId, creatorId))
              .thenThrow(new MemberNotFoundException());

          mockMvc.perform(
                  post("/creators/" + creatorId + "/subscribe")
                      .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                      .characterEncoding("UTF-8"))
              .andExpect(status().isNotFound())
              .andDo(print())
              .andExpect(response -> Assertions.assertTrue(
                  response.getResolvedException() instanceof MemberNotFoundException));
        }

        @Test
        @DisplayName("크리에이터에 대해 구독버튼을 누르면 404 NOT FOUND 를 반환한다.")
        void throwsCreatorNotFoundException() throws Exception {
          Long loginMemberId = 1L;
          Long wrongCreatorId = 1L;

          when(subscriptionService.subscribeOrUnsubscribeCreator(loginMemberId, wrongCreatorId))
              .thenThrow(new CreatorNotFoundException());

          mockMvc.perform(
                  post("/creators/" + wrongCreatorId + "/subscribe")
                      .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                      .characterEncoding("UTF-8"))
              .andExpect(status().isNotFound())
              .andDo(print())
              .andExpect(response -> Assertions.assertTrue(
                  response.getResolvedException() instanceof CreatorNotFoundException));
        }
      }


    }
  }

  @Nested
  @DisplayName("구독 피드 조회 API는")
  class SubscriptionCreatorContentsRetrievalTest {

    private GetContentsCommonResponse setUpForRetrieval() {
      List<ContentViewerRecommendationResponse> contentViewerRecommendationResponses = List.of(
          new ContentViewerRecommendationResponse("네이버", "https://naverlogo.com"));
      List<ContentViewerRecommendationResponse> contentViewerRecommendationResponses2 = List.of(
          new ContentViewerRecommendationResponse("네이버", "https://naverlogo.com"),
          new ContentViewerRecommendationResponse("카카오", "https://kakaologo.com"));
      ContentResponse contentResponse = new ContentResponse(1L, "개발자의 삶", "개발왕김딴딴", 2L,
          "https://img1.com", "https://okky.kr/articles/503803", 4,
          100, false, false, "2023-02-17T12:30.334", contentViewerRecommendationResponses);
      ContentResponse contentResponse2 = new ContentResponse(2L, "당신은 개발자가 맞는가?", "개발왕김딴딴", 2L,
          "https://img2.com", "https://okky.kr/articles/503343", 1,
          35, false, false, "2023-02-17T12:30.334", contentViewerRecommendationResponses2);
      List<ContentResponse> contentResponses = List.of(contentResponse, contentResponse2);
      GetContentsCommonResponse getContentsCommonResponse = new GetContentsCommonResponse(
          contentResponses, true);
      return getContentsCommonResponse;
    }

    @Nested
    @DisplayName("카테고리 별 조회 시")
    class RetrievalByCategory {

      @BeforeEach
      void setUp() {
        authSetup();
      }

      @Nested
      @DisplayName("[성공]")
      class Success {

        @Test
        @DisplayName("해당 카테고리에 속한 구독 크리에이터의 글을 볼 수 있다.")
        void retrieveSubscribeCreatorContentsByCategory() throws Exception {
          String page = "0";
          String size = "10";

          GetContentsCommonResponse getContentsCommonResponse = setUpForRetrieval();

          Long memberId = 1L;
          String category = "개발";
          Pageable pageable = PageRequest.of(Integer.parseInt(page), Integer.parseInt(size));

          doReturn(getContentsCommonResponse)
              .when(subscriptionService).retrieveSubscribedCreatorsContentsByCategoryId(memberId,
                  category, pageable);

          mockMvc.perform(
                  get("/subscriptions/contents")
                      .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                      .param("page", page)
                      .param("size", size)
                      .param("category", category)
                      .characterEncoding("UTF-8"))
              .andExpect(status().isOk())
              .andDo(print())
              .andDo(document("SubscriptionControllerTest/retrieveByCategory",
                  preprocessRequest(prettyPrint()),
                  preprocessResponse(prettyPrint()),
                  requestHeaders(
                      headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")
                  ),
                  responseFields(
                      fieldWithPath("contents.[].contentId").type(JsonFieldType.NUMBER)
                          .description("컨텐츠 id"),
                      fieldWithPath("contents.[].title").type(JsonFieldType.STRING)
                          .description("컨텐츠 제목"),
                      fieldWithPath("contents.[].creatorName").type(JsonFieldType.STRING)
                          .description("크리에이터 이름"),
                      fieldWithPath("contents.[].creatorId").type(JsonFieldType.NUMBER)
                          .description("크리에이터 id"),
                      fieldWithPath("contents.[].contentImgUrl").type(JsonFieldType.STRING)
                          .description("컨텐츠 이미지 URL"),
                      fieldWithPath("contents.[].link").type(JsonFieldType.STRING)
                          .description("컨텐츠 연결 외부 링크"),
                      fieldWithPath("contents.[].likeCount").type(JsonFieldType.NUMBER)
                          .description("좋아요 개수"),
                      fieldWithPath("contents.[].viewCount").type(JsonFieldType.NUMBER)
                          .description("조회수 개수"),
                      fieldWithPath("contents.[].isBookmarked").type(JsonFieldType.BOOLEAN)
                          .description("북마크 여부"),
                      fieldWithPath("contents.[].isLiked").type(JsonFieldType.BOOLEAN)
                          .description("좋아요 여부"),
                      fieldWithPath("contents.[].createdAt").type(JsonFieldType.STRING)
                          .description("컨텐츠 생성 날짜"),
                      fieldWithPath("contents.[].recommendations").type(
                          JsonFieldType.ARRAY).description("추천 배열"),
                      fieldWithPath("contents.[].recommendations.[].bannerName").type(
                          JsonFieldType.STRING).description("배너명"),
                      fieldWithPath(
                          "contents.[].recommendations.[].bannerLogoImgUrl").type(
                          JsonFieldType.STRING).description("배너 로고 URL"),
                      fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
                          .description("다음 페이지 존재여부")
                  )
              ));
        }
      }

      @Nested
      @DisplayName("[실패]")
      class Fail {
        @Test
        @DisplayName("요청 파라미터가 누락될 경우 MissingServletRequestParameterException이 발생한다.")
        void retrieveSubscribeCreatorContentsByCategory() throws Exception {
          String page = null;
          String size = null;
          String category = null;

          mockMvc.perform(
                  get("/subscriptions/contents")
                      .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                      .param("page", page)
                      .param("size", size)
                      .param("category", category)
                      .characterEncoding("UTF-8"))
              .andExpect(status().isBadRequest())
              .andExpect(
                  response -> Assertions.assertTrue(
                      response.getResolvedException() instanceof MissingServletRequestParameterException));

        }
      }
    }

    @Nested
    @DisplayName("전체 카테고리 조회 시")
    class RetrievalForAllCategory {

      @BeforeEach
      void setUp() {
        authSetup();
      }

      @Nested
      @DisplayName("[성공]")
      class Success {

        @Test
        @DisplayName("구독 크리에이터의 모든 카테고리에 대한 글을 조회할 수 있다.")
        void retrieveSubscribedCreatorsAllCategoryContents() throws Exception {
          String page = "0";
          String size = "10";
          GetContentsCommonResponse getContentsCommonResponse = setUpForRetrieval();
          Long memberId = 1L;
          Pageable pageable = PageRequest.of(Integer.parseInt(page), Integer.parseInt(size));

          doReturn(getContentsCommonResponse)
              .when(subscriptionService).retrieveSubscribedCreatorsContentsForAllCategories(memberId,
                  pageable);

          mockMvc.perform(
                  get("/subscriptions/contents/all")
                      .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                      .param("page", page)
                      .param("size", size)
                      .characterEncoding("UTF-8"))
              .andExpect(status().isOk())
              .andDo(print())
              .andDo(document("SubscriptionControllerTest/retrieveAllCategory",
                  preprocessRequest(prettyPrint()),
                  preprocessResponse(prettyPrint()),
                  requestHeaders(
                      headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")
                  ),
                  responseFields(
                      fieldWithPath("contents.[].contentId").type(JsonFieldType.NUMBER)
                          .description("컨텐츠 id"),
                      fieldWithPath("contents.[].title").type(JsonFieldType.STRING)
                          .description("컨텐츠 제목"),
                      fieldWithPath("contents.[].creatorName").type(JsonFieldType.STRING)
                          .description("크리에이터 이름"),
                      fieldWithPath("contents.[].creatorId").type(JsonFieldType.NUMBER)
                          .description("크리에이터 id"),
                      fieldWithPath("contents.[].contentImgUrl").type(JsonFieldType.STRING)
                          .description("컨텐츠 이미지 URL"),
                      fieldWithPath("contents.[].link").type(JsonFieldType.STRING)
                          .description("컨텐츠 연결 외부 링크"),
                      fieldWithPath("contents.[].likeCount").type(JsonFieldType.NUMBER)
                          .description("좋아요 개수"),
                      fieldWithPath("contents.[].viewCount").type(JsonFieldType.NUMBER)
                          .description("조회수 개수"),
                      fieldWithPath("contents.[].isBookmarked").type(JsonFieldType.BOOLEAN)
                          .description("북마크 여부"),
                      fieldWithPath("contents.[].isLiked").type(JsonFieldType.BOOLEAN)
                          .description("좋아요 여부"),
                      fieldWithPath("contents.[].createdAt").type(JsonFieldType.STRING)
                          .description("컨텐츠 생성 날짜"),
                      fieldWithPath("contents.[].recommendations").type(
                          JsonFieldType.ARRAY).description("추천 배열"),
                      fieldWithPath("contents.[].recommendations.[].bannerName").type(
                          JsonFieldType.STRING).description("배너명"),
                      fieldWithPath(
                          "contents.[].recommendations.[].bannerLogoImgUrl").type(
                          JsonFieldType.STRING).description("배너 로고 URL"),
                      fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
                          .description("다음 페이지 존재여부")
                  )
              ));
        }
      }

      @Nested
      @DisplayName("[실패]")
      class Fail {
        @Test
        @DisplayName("요청 파라미터가 누락될 경우 MissingServletRequestParameterException이 발생한다.")
        void retrieveSubscribeCreatorContentsByCategory() throws Exception {
          String page = null;
          String size = null;

          mockMvc.perform(
                  get("/subscriptions/contents/all")
                      .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                      .param("page", page)
                      .param("size", size)
                      .characterEncoding("UTF-8"))
              .andExpect(status().isBadRequest())
              .andExpect(
                  response -> Assertions.assertTrue(
                      response.getResolvedException() instanceof MissingServletRequestParameterException));

        }
      }
    }
  }
}
