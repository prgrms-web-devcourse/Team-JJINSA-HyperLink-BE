package com.hyperlink.server.content.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.AuthSetupForMock;
import com.hyperlink.server.AdminAuthSetupForMock;
import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.content.controller.ContentController;
import com.hyperlink.server.domain.content.dto.ContentAdminResponse;
import com.hyperlink.server.domain.content.dto.ContentAdminResponses;
import com.hyperlink.server.domain.content.dto.ContentResponse;
import com.hyperlink.server.domain.content.dto.GetContentsCommonResponse;
import com.hyperlink.server.domain.content.dto.ContentViewerRecommendationResponse;
import com.hyperlink.server.domain.content.dto.SearchResponse;
import com.hyperlink.server.domain.content.exception.CategoryAndCreatorIdConstraintViolationException;
import com.hyperlink.server.domain.content.exception.ContentNotFoundException;
import java.util.List;
import java.util.stream.Stream;
import javax.validation.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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

@WebMvcTest(ContentController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public class ContentControllerTest extends AdminAuthSetupForMock {

  @MockBean
  ContentService contentService;
  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;

  @Nested
  @DisplayName("조회수 추가 API는")
  class AddInquiryOfContent {

    @Nested
    @DisplayName("유효한 요청값이 들어오면")
    class Success {

      Long contentId = 1L;

      @Test
      @DisplayName("조회수 추가에 성공하고 OK와 최종 조회수를 응답한다")
      void addInquiryOfContentTest() throws Exception {
        mockMvc.perform(
                patch("/contents/" + contentId + "/view")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            )
            .andExpect(status().isOk())
            .andDo(
                document(
                    "ContentControllerTest/addInquiryOfContent",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("jwt header")
                    ),
                    responseFields(
                        fieldWithPath("viewCount").type(JsonFieldType.NUMBER)
                            .description("조회수 추가 완료 후 최종 조회수")
                    )
                )
            );
      }
    }
  }

  @Nested
  @DisplayName("검색 API는")
  class SearchContent {

    String page = "0", size = "10";

    @Nested
    @DisplayName("keyword에 공백이 들어오면")
    class NullOrBlank {

      @ParameterizedTest
      @EmptySource
      @DisplayName("BadRequest를 응답한다")
      void badRequest(String keyword) throws Exception {
        mockMvc.perform(
                get("/contents/search")
                    .param("keyword", keyword)
                    .param("page", page)
                    .param("size", size)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .characterEncoding("UTF-8")
            )
            .andExpect(status().isBadRequest())
            .andExpect(response -> Assertions.assertTrue(
                response.getResolvedException() instanceof ValidationException));
      }
    }

    @Nested
    @DisplayName("유효한 요청값이 들어오면")
    class Success {

      String keyword = "개발 성장";

      @Test
      @DisplayName("검색결과 리스트를 응답한다")
      void searchContentsTest() throws Exception {
        authSetup();

        List<ContentViewerRecommendationResponse> contentViewerRecommendationResponses = List.of(
            new ContentViewerRecommendationResponse("네이버", "https://imglogo.com"));
        ContentResponse contentResponse = new ContentResponse(27L, "개발자의 삶", "슈카", 2L,
            "https://img1.com", "https://okky.kr/articles/503803", 4,
            100, false, false, "2023-02-17T12:30.334", contentViewerRecommendationResponses);
        List<ContentResponse> contentResponses = List.of(contentResponse);
        GetContentsCommonResponse getContentsCommonResponse = new GetContentsCommonResponse(
            contentResponses, true);
        SearchResponse searchResponse = new SearchResponse(getContentsCommonResponse, keyword, 4);

        Long memberId = 1L;
        Pageable pageable = PageRequest.of(Integer.parseInt(page), Integer.parseInt(size));
        doReturn(searchResponse).when(contentService).search(memberId, keyword, pageable);

        mockMvc.perform(
                get("/contents/search")
                    .param("keyword", keyword)
                    .param("page", page)
                    .param("size", size)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .characterEncoding("UTF-8")
            ).andExpect(status().isOk())
            .andDo(
                document(
                    "ContentControllerTest/search",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("jwt header")
                    ),
                    responseFields(
                        fieldWithPath("getContentsCommonResponse.contents.[].contentId").type(
                                JsonFieldType.NUMBER)
                            .description("컨텐츠 id"),
                        fieldWithPath("getContentsCommonResponse.contents.[].title").type(
                                JsonFieldType.STRING)
                            .description("컨텐츠 제목"),
                        fieldWithPath("getContentsCommonResponse.contents.[].creatorName").type(
                                JsonFieldType.STRING)
                            .description("크리에이터 이름"),
                        fieldWithPath("getContentsCommonResponse.contents.[].creatorId").type(
                                JsonFieldType.NUMBER)
                            .description("크리에이터 id"),
                        fieldWithPath("getContentsCommonResponse.contents.[].contentImgUrl").type(
                                JsonFieldType.STRING)
                            .description("컨텐츠 이미지 URL"),
                        fieldWithPath("getContentsCommonResponse.contents.[].link").type(
                                JsonFieldType.STRING)
                            .description("컨텐츠 연결 외부 링크"),
                        fieldWithPath("getContentsCommonResponse.contents.[].likeCount").type(
                                JsonFieldType.NUMBER)
                            .description("좋아요 개수"),
                        fieldWithPath("getContentsCommonResponse.contents.[].viewCount").type(
                                JsonFieldType.NUMBER)
                            .description("조회수 개수"),
                        fieldWithPath("getContentsCommonResponse.contents.[].isBookmarked").type(
                                JsonFieldType.BOOLEAN)
                            .description("북마크 여부"),
                        fieldWithPath("getContentsCommonResponse.contents.[].isLiked").type(
                                JsonFieldType.BOOLEAN)
                            .description("좋아요 여부"),
                        fieldWithPath("getContentsCommonResponse.contents.[].createdAt").type(
                                JsonFieldType.STRING)
                            .description("컨텐츠 생성 날짜"),
                        fieldWithPath("getContentsCommonResponse.contents.[].recommendations").type(
                            JsonFieldType.ARRAY).description("회사 추천 배열"),
                        fieldWithPath(
                            "getContentsCommonResponse.contents.[].recommendations.[].bannerName").type(
                            JsonFieldType.STRING).description("회사명"),
                        fieldWithPath(
                            "getContentsCommonResponse.contents.[].recommendations.[].bannerLogoImgUrl").type(
                            JsonFieldType.STRING).description("회사 로고 URL"),
                        fieldWithPath("getContentsCommonResponse.hasNext").type(
                                JsonFieldType.BOOLEAN)
                            .description("다음 페이지 존재여부"),
                        fieldWithPath("keyword").type(JsonFieldType.STRING).description("검색 키워드"),
                        fieldWithPath("resultCount").type(JsonFieldType.NUMBER)
                            .description("검색 결과 전체 개수")
                    )
                )
            );
      }
    }
  }

  @Nested
  @DisplayName("[Admin] 컨텐츠 활성화 API는")
  class ActivateContent {

    @BeforeEach
    void setUp() {
      authSetup();
    }

    @Nested
    @DisplayName("올바른 ContentId가 들어오면")
    class Success {

      @Test
      @DisplayName("해당 글을 Viewable로 변경한다.")
      void makeContentViewable() throws Exception {
        Long contentId = 1L;

        doNothing().when(contentService).activateContent(contentId);

        mockMvc.perform(post("/admin/contents/" + contentId + "/activate")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(document(
                "ContentControllerTest/activateContent",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")
                )
            ));
      }
    }

    @Nested
    @DisplayName("올바르지 않은 ContentId가 들어오면")
    class Fail {

      @Test
      @DisplayName("404 NOT FOUND를 반환하고 ContentNotFoundException이 발생한다.")
      void activateIsViewableFail404ContentNotFoundException() throws Exception {
        Long contentId = 1L;

        doThrow(new ContentNotFoundException()).when(contentService).activateContent(contentId);

        mockMvc.perform(post("/admin/contents/" + contentId + "/activate")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
            .andExpect(status().isNotFound())
            .andExpect(response -> Assertions.assertTrue(
                response.getResolvedException() instanceof ContentNotFoundException))
            .andDo(print());
      }
    }


  }

  @Nested
  @DisplayName("트렌드, 크링에이터별 컨텐츠 조회 API는")
  class RetrieveContent {

    String page = "0";
    String size = "10";

    @BeforeEach
    void setUp() {
      authSetup();
    }

    @TestInstance(Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("트렌드 게시글")
    class Trend {

      Stream<Arguments> createInput() {
        return Stream.of(
            Arguments.of("develop", null, "popular"),
            Arguments.of("develop", null, "recent")
        );
      }

      @ParameterizedTest
      @MethodSource("createInput")
      @DisplayName("[category]와 [sort] 를 입력받으면 인기순, 최신순 트렌드를 조회한다.")
      void retrievePopularTrend(String category, Long creatorId, String sort) throws Exception {
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

        Long memberId = 1L;
        Pageable pageable = PageRequest.of(Integer.parseInt(page), Integer.parseInt(size));

        doReturn(getContentsCommonResponse).when(contentService)
            .retrieveTrendContents(memberId, category, sort, pageable);

        mockMvc.perform(
                get("/contents")
                    .param("sort", sort)
                    .param("page", page)
                    .param("size", size)
                    .param("category", category)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .characterEncoding("UTF-8")
            ).andExpect(status().isOk())
            .andDo(print())
            .andDo(
                document(
                    "ContentControllerTest/retrieve",
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
                )
            );
      }

      @ParameterizedTest
      @ValueSource(strings = {"popular", "recent"})
      @DisplayName("전체 카테고리에 대해서 인기순, 최신순 트렌드를 조회한다.")
      void retrieveTrendForAllCategories(String sort) throws Exception {
        List<ContentViewerRecommendationResponse> recommendationMemberInfoRespons = List.of(
            new ContentViewerRecommendationResponse("네이버", "https://naverlogo.com"));
        List<ContentViewerRecommendationResponse> contentViewerRecommendationResponses2 = List.of(
            new ContentViewerRecommendationResponse("네이버", "https://naverlogo.com"),
            new ContentViewerRecommendationResponse("카카오", "https://kakaologo.com"));
        ContentResponse contentResponse = new ContentResponse(1L, "개발자의 삶", "개발왕김딴딴", 2L,
            "https://img1.com", "https://okky.kr/articles/503803", 4,
            100, false, false, "2023-02-17T12:30.334", recommendationMemberInfoRespons);
        ContentResponse contentResponse2 = new ContentResponse(2L, "당신은 개발자가 맞는가?", "개발왕김딴딴", 2L,
            "https://img2.com", "https://okky.kr/articles/503343", 1,
            35, false, false, "2023-02-17T12:30.334", contentViewerRecommendationResponses2);
        List<ContentResponse> contentResponses = List.of(contentResponse, contentResponse2);
        GetContentsCommonResponse getContentsCommonResponse = new GetContentsCommonResponse(
            contentResponses, true);

        Long memberId = 1L;
        Pageable pageable = PageRequest.of(Integer.parseInt(page), Integer.parseInt(size));

        doReturn(getContentsCommonResponse).when(contentService)
            .retrieveTrendAllCategoriesContents(memberId, sort, pageable);

        mockMvc.perform(
                get("/contents/all")
                    .param("sort", sort)
                    .param("page", page)
                    .param("size", size)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .characterEncoding("UTF-8")
            ).andExpect(status().isOk())
            .andDo(print())
            .andDo(
                document(
                    "ContentControllerTest/retrieveAllCategory",
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
                )
            );
      }
    }

    @TestInstance(Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("크리에이터 별 게시글")
    class CreatorContent {

      Stream<Arguments> createInput() {
        return Stream.of(
            Arguments.of(null, "2", "popular"),
            Arguments.of(null, "2", "recent")
        );
      }

      @ParameterizedTest
      @MethodSource("createInput")
      @DisplayName("[creatorId]와 [sort] 를 입력받으면 인기순 트렌드를 조회한다.")
      void retrievePopularTrend(String category, String creatorId, String sort) throws Exception {
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

        Long memberId = 1L;
        Pageable pageable = PageRequest.of(Integer.parseInt(page), Integer.parseInt(size));

        doReturn(getContentsCommonResponse).when(contentService)
            .retrieveTrendContents(memberId, category, sort, pageable);
        doReturn(getContentsCommonResponse).when(contentService)
            .retrieveCreatorContents(memberId, Long.parseLong(creatorId), sort, pageable);

        mockMvc.perform(
            get("/contents")
                .param("sort", sort)
                .param("page", page)
                .param("size", size)
                .param("creatorId", creatorId)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .characterEncoding("UTF-8")
        ).andExpect(status().isOk());
      }
    }

    @Nested
    @DisplayName("[실패]")
    class Fail {

      @Test
      @DisplayName("category와 creatorId를 동시에 입력하면 CategoryAndCreatorIdConstraintViolationException이 발생한다.")
      void bothCategoryAndCreatorId() throws Exception {
        String category = "개발";
        String creatorId = "2";
        String page = "0";
        String size = "10";
        String sort = "recent";

        mockMvc.perform(
                get("/contents")
                    .param("sort", sort)
                    .param("page", page)
                    .param("size", size)
                    .param("creatorId", creatorId)
                    .param("category", category)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .characterEncoding("UTF-8")
            ).andExpect(status().isBadRequest())
            .andExpect(response -> Assertions.assertTrue(
                response.getResolvedException() instanceof CategoryAndCreatorIdConstraintViolationException));

      }

      @Test
      @DisplayName("파라미터 값이 유효하지 않거나 필수 파라미터가 입력되지 않으면 MissingServletRequestParameterException이 발생한다.")
      void missingArgumentOrInvalidArgument() throws Exception {
        String creatorId = "-1";
        String page = null;
        String size = null;
        String sort = null;

        mockMvc.perform(
                get("/contents")
                    .param("sort", sort)
                    .param("page", page)
                    .param("size", size)
                    .param("creatorId", creatorId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .characterEncoding("UTF-8")
            ).andExpect(status().isBadRequest())
            .andExpect(response -> Assertions.assertTrue(
                response.getResolvedException() instanceof MissingServletRequestParameterException));

      }
    }
  }

  @Nested
  @DisplayName("[Admin] 비활성 컨텐츠 조회 API는")
  class RetrieveInactivateContent {

    @BeforeEach
    void setUp() {
      authSetup();
    }

    @Nested
    @DisplayName("[성공]")
    class Success {

      @Test
      @DisplayName("비활성된 컨텐츠를 조회한다.")
      void retrieveInactiveContents() throws Exception {
        String page = "0";
        String size = "10";

        ContentAdminResponse content1 = new ContentAdminResponse(1L, "컨텐츠 제목1",
            "https://d2.naver.com/aaa");
        ContentAdminResponse content2 = new ContentAdminResponse(2L, "컨텐츠 제목2",
            "https://d2.naver.com/bbb");
        ContentAdminResponse content3 = new ContentAdminResponse(3L, "컨텐츠 제목3",
            "https://d2.naver.com/ccc");
        ContentAdminResponse content4 = new ContentAdminResponse(4L, "컨텐츠 제목4",
            "https://d2.naver.com/ddd");
        ContentAdminResponses contentAdminResponses = new ContentAdminResponses(
            List.of(content1, content2, content3, content4), 0, 1);

        when(contentService.retrieveInactivatedContents(any())).thenReturn(contentAdminResponses);

        mockMvc.perform(
                get("/admin/contents")
                    .param("page", page)
                    .param("size", size)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .characterEncoding("UTF-8")
            ).andExpect(status().isOk())
            .andDo(print())
            .andDo(
                document(
                    "ContentControllerTest/retrieveInactiveContents",
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
                        fieldWithPath("contents.[].link").type(JsonFieldType.STRING)
                            .description("컨텐츠 연결 외부 링크"),
                        fieldWithPath("currentPage").type(JsonFieldType.NUMBER)
                            .description("현재 페이지 번호"),
                        fieldWithPath("totalPage").type(JsonFieldType.NUMBER)
                            .description("전체 페이지 번호")
                    )
                )
            );
      }
    }

  }

  @Nested
  @DisplayName("[Admin] 컨텐츠 삭제 API는")
  class DeleteContentByAdmin {

    @BeforeEach
    void setUp() {
      authSetup();
    }

    @Nested
    @DisplayName("[성공]")
    class Success {

      @Test
      @DisplayName("컨텐츠를 삭제한다.")
      void deleteContentById() throws Exception {
        Long contentId = 1L;

        doNothing().when(contentService).deleteContentsById(contentId);

        mockMvc.perform(
                delete("/admin/contents/" + contentId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .characterEncoding("UTF-8")
            ).andExpect(status().isOk())
            .andDo(print())
            .andDo(
                document(
                    "ContentControllerTest/deleteContentByAdmin",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")
                    )
                )
            );
      }
    }

  }
}
