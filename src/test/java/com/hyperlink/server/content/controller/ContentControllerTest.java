package com.hyperlink.server.content.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
import com.hyperlink.server.AdminAuthSetupForMock;
import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.content.controller.ContentController;
import com.hyperlink.server.domain.content.dto.ContentAdminResponse;
import com.hyperlink.server.domain.content.dto.ContentAdminResponses;
import com.hyperlink.server.domain.content.dto.ContentResponse;
import com.hyperlink.server.domain.content.dto.ContentViewerRecommendationResponse;
import com.hyperlink.server.domain.content.dto.GetContentsCommonResponse;
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
  @DisplayName("????????? ?????? API???")
  class AddInquiryOfContent {

    Long contentId = 1L;

    @Nested
    @DisplayName("is_search param??? null??????")
    class Fail {
      @Test
      @DisplayName("BadRequest??? ????????????")
      void isSearchIsNull() throws Exception {
        authSetup();

        String isSearch = null;

        mockMvc.perform(
                patch("/contents/" + contentId + "/view")
                    .param("search", isSearch)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            )
            .andExpect(status().isBadRequest())
            .andExpect(response -> assertTrue(
                response.getResolvedException() instanceof MissingServletRequestParameterException));
      }
    }

    @Nested
    @DisplayName("????????? ???????????? ????????????")
    class Success {

      @Test
      @DisplayName("????????? ????????? ???????????? OK??? ?????? ???????????? ????????????")
      void addInquiryOfContentTest() throws Exception {
        mockMvc.perform(
                patch("/contents/" + contentId + "/view")
                    .param("search", "0")
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
                            .description("????????? ?????? ?????? ??? ?????? ?????????")
                    )
                )
            );
      }
    }
  }

  @Nested
  @DisplayName("?????? API???")
  class SearchContent {

    String page = "0", size = "10";

    @Nested
    @DisplayName("keyword??? ????????? ????????????")
    class NullOrBlank {

      @ParameterizedTest
      @EmptySource
      @DisplayName("BadRequest??? ????????????")
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
    @DisplayName("????????? ???????????? ????????????")
    class Success {

      String keyword = "?????? ??????";

      @Test
      @DisplayName("???????????? ???????????? ????????????")
      void searchContentsTest() throws Exception {
        authSetup();

        List<ContentViewerRecommendationResponse> contentViewerRecommendationResponses = List.of(
            new ContentViewerRecommendationResponse("?????????", "https://imglogo.com"));
        ContentResponse contentResponse = new ContentResponse(27L, "???????????? ???", "??????", 2L,
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
                            .description("????????? id"),
                        fieldWithPath("getContentsCommonResponse.contents.[].title").type(
                                JsonFieldType.STRING)
                            .description("????????? ??????"),
                        fieldWithPath("getContentsCommonResponse.contents.[].creatorName").type(
                                JsonFieldType.STRING)
                            .description("??????????????? ??????"),
                        fieldWithPath("getContentsCommonResponse.contents.[].creatorId").type(
                                JsonFieldType.NUMBER)
                            .description("??????????????? id"),
                        fieldWithPath("getContentsCommonResponse.contents.[].contentImgUrl").type(
                                JsonFieldType.STRING)
                            .description("????????? ????????? URL"),
                        fieldWithPath("getContentsCommonResponse.contents.[].link").type(
                                JsonFieldType.STRING)
                            .description("????????? ?????? ?????? ??????"),
                        fieldWithPath("getContentsCommonResponse.contents.[].likeCount").type(
                                JsonFieldType.NUMBER)
                            .description("????????? ??????"),
                        fieldWithPath("getContentsCommonResponse.contents.[].viewCount").type(
                                JsonFieldType.NUMBER)
                            .description("????????? ??????"),
                        fieldWithPath("getContentsCommonResponse.contents.[].isBookmarked").type(
                                JsonFieldType.BOOLEAN)
                            .description("????????? ??????"),
                        fieldWithPath("getContentsCommonResponse.contents.[].isLiked").type(
                                JsonFieldType.BOOLEAN)
                            .description("????????? ??????"),
                        fieldWithPath("getContentsCommonResponse.contents.[].createdAt").type(
                                JsonFieldType.STRING)
                            .description("????????? ?????? ??????"),
                        fieldWithPath("getContentsCommonResponse.contents.[].recommendations").type(
                            JsonFieldType.ARRAY).description("?????? ?????? ??????"),
                        fieldWithPath(
                            "getContentsCommonResponse.contents.[].recommendations.[].bannerName").type(
                            JsonFieldType.STRING).description("?????????"),
                        fieldWithPath(
                            "getContentsCommonResponse.contents.[].recommendations.[].bannerLogoImgUrl").type(
                            JsonFieldType.STRING).description("?????? ?????? URL"),
                        fieldWithPath("getContentsCommonResponse.hasNext").type(
                                JsonFieldType.BOOLEAN)
                            .description("?????? ????????? ????????????"),
                        fieldWithPath("keyword").type(JsonFieldType.STRING).description("?????? ?????????"),
                        fieldWithPath("resultCount").type(JsonFieldType.NUMBER)
                            .description("?????? ?????? ?????? ??????")
                    )
                )
            );
      }
    }
  }

  @Nested
  @DisplayName("[Admin] ????????? ????????? API???")
  class ActivateContent {

    @BeforeEach
    void setUp() {
      authSetup();
    }

    @Nested
    @DisplayName("????????? ContentId??? ????????????")
    class Success {

      @Test
      @DisplayName("?????? ?????? Viewable??? ????????????.")
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
    @DisplayName("???????????? ?????? ContentId??? ????????????")
    class Fail {

      @Test
      @DisplayName("404 NOT FOUND??? ???????????? ContentNotFoundException??? ????????????.")
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
  @DisplayName("?????????, ?????????????????? ????????? ?????? API???")
  class RetrieveContent {

    String page = "0";
    String size = "10";

    @BeforeEach
    void setUp() {
      authSetup();
    }

    @TestInstance(Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("????????? ?????????")
    class Trend {

      Stream<Arguments> createInput() {
        return Stream.of(
            Arguments.of("develop", null, "popular"),
            Arguments.of("develop", null, "recent")
        );
      }

      @ParameterizedTest
      @MethodSource("createInput")
      @DisplayName("[category]??? [sort] ??? ??????????????? ?????????, ????????? ???????????? ????????????.")
      void retrievePopularTrend(String category, Long creatorId, String sort) throws Exception {
        List<ContentViewerRecommendationResponse> contentViewerRecommendationResponses = List.of(
            new ContentViewerRecommendationResponse("?????????", "https://naverlogo.com"));
        List<ContentViewerRecommendationResponse> contentViewerRecommendationResponses2 = List.of(
            new ContentViewerRecommendationResponse("?????????", "https://naverlogo.com"),
            new ContentViewerRecommendationResponse("?????????", "https://kakaologo.com"));
        ContentResponse contentResponse = new ContentResponse(1L, "???????????? ???", "??????????????????", 2L,
            "https://img1.com", "https://okky.kr/articles/503803", 4,
            100, false, false, "2023-02-17T12:30.334", contentViewerRecommendationResponses);
        ContentResponse contentResponse2 = new ContentResponse(2L, "????????? ???????????? ??????????", "??????????????????", 2L,
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
                            .description("????????? id"),
                        fieldWithPath("contents.[].title").type(JsonFieldType.STRING)
                            .description("????????? ??????"),
                        fieldWithPath("contents.[].creatorName").type(JsonFieldType.STRING)
                            .description("??????????????? ??????"),
                        fieldWithPath("contents.[].creatorId").type(JsonFieldType.NUMBER)
                            .description("??????????????? id"),
                        fieldWithPath("contents.[].contentImgUrl").type(JsonFieldType.STRING)
                            .description("????????? ????????? URL"),
                        fieldWithPath("contents.[].link").type(JsonFieldType.STRING)
                            .description("????????? ?????? ?????? ??????"),
                        fieldWithPath("contents.[].likeCount").type(JsonFieldType.NUMBER)
                            .description("????????? ??????"),
                        fieldWithPath("contents.[].viewCount").type(JsonFieldType.NUMBER)
                            .description("????????? ??????"),
                        fieldWithPath("contents.[].isBookmarked").type(JsonFieldType.BOOLEAN)
                            .description("????????? ??????"),
                        fieldWithPath("contents.[].isLiked").type(JsonFieldType.BOOLEAN)
                            .description("????????? ??????"),
                        fieldWithPath("contents.[].createdAt").type(JsonFieldType.STRING)
                            .description("????????? ?????? ??????"),
                        fieldWithPath("contents.[].recommendations").type(
                            JsonFieldType.ARRAY).description("?????? ??????"),
                        fieldWithPath("contents.[].recommendations.[].bannerName").type(
                            JsonFieldType.STRING).description("?????????"),
                        fieldWithPath(
                            "contents.[].recommendations.[].bannerLogoImgUrl").type(
                            JsonFieldType.STRING).description("?????? ?????? URL"),
                        fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
                            .description("?????? ????????? ????????????")
                    )
                )
            );
      }

      @ParameterizedTest
      @ValueSource(strings = {"popular", "recent"})
      @DisplayName("?????? ??????????????? ????????? ?????????, ????????? ???????????? ????????????.")
      void retrieveTrendForAllCategories(String sort) throws Exception {
        List<ContentViewerRecommendationResponse> recommendationMemberInfoRespons = List.of(
            new ContentViewerRecommendationResponse("?????????", "https://naverlogo.com"));
        List<ContentViewerRecommendationResponse> contentViewerRecommendationResponses2 = List.of(
            new ContentViewerRecommendationResponse("?????????", "https://naverlogo.com"),
            new ContentViewerRecommendationResponse("?????????", "https://kakaologo.com"));
        ContentResponse contentResponse = new ContentResponse(1L, "???????????? ???", "??????????????????", 2L,
            "https://img1.com", "https://okky.kr/articles/503803", 4,
            100, false, false, "2023-02-17T12:30.334", recommendationMemberInfoRespons);
        ContentResponse contentResponse2 = new ContentResponse(2L, "????????? ???????????? ??????????", "??????????????????", 2L,
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
                            .description("????????? id"),
                        fieldWithPath("contents.[].title").type(JsonFieldType.STRING)
                            .description("????????? ??????"),
                        fieldWithPath("contents.[].creatorName").type(JsonFieldType.STRING)
                            .description("??????????????? ??????"),
                        fieldWithPath("contents.[].creatorId").type(JsonFieldType.NUMBER)
                            .description("??????????????? id"),
                        fieldWithPath("contents.[].contentImgUrl").type(JsonFieldType.STRING)
                            .description("????????? ????????? URL"),
                        fieldWithPath("contents.[].link").type(JsonFieldType.STRING)
                            .description("????????? ?????? ?????? ??????"),
                        fieldWithPath("contents.[].likeCount").type(JsonFieldType.NUMBER)
                            .description("????????? ??????"),
                        fieldWithPath("contents.[].viewCount").type(JsonFieldType.NUMBER)
                            .description("????????? ??????"),
                        fieldWithPath("contents.[].isBookmarked").type(JsonFieldType.BOOLEAN)
                            .description("????????? ??????"),
                        fieldWithPath("contents.[].isLiked").type(JsonFieldType.BOOLEAN)
                            .description("????????? ??????"),
                        fieldWithPath("contents.[].createdAt").type(JsonFieldType.STRING)
                            .description("????????? ?????? ??????"),
                        fieldWithPath("contents.[].recommendations").type(
                            JsonFieldType.ARRAY).description("?????? ??????"),
                        fieldWithPath("contents.[].recommendations.[].bannerName").type(
                            JsonFieldType.STRING).description("?????????"),
                        fieldWithPath(
                            "contents.[].recommendations.[].bannerLogoImgUrl").type(
                            JsonFieldType.STRING).description("?????? ?????? URL"),
                        fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
                            .description("?????? ????????? ????????????")
                    )
                )
            );
      }
    }

    @TestInstance(Lifecycle.PER_CLASS)
    @Nested
    @DisplayName("??????????????? ??? ?????????")
    class CreatorContent {

      Stream<Arguments> createInput() {
        return Stream.of(
            Arguments.of(null, "2", "popular"),
            Arguments.of(null, "2", "recent")
        );
      }

      @ParameterizedTest
      @MethodSource("createInput")
      @DisplayName("[creatorId]??? [sort] ??? ??????????????? ????????? ???????????? ????????????.")
      void retrievePopularTrend(String category, String creatorId, String sort) throws Exception {
        List<ContentViewerRecommendationResponse> contentViewerRecommendationResponses = List.of(
            new ContentViewerRecommendationResponse("?????????", "https://naverlogo.com"));
        List<ContentViewerRecommendationResponse> contentViewerRecommendationResponses2 = List.of(
            new ContentViewerRecommendationResponse("?????????", "https://naverlogo.com"),
            new ContentViewerRecommendationResponse("?????????", "https://kakaologo.com"));
        ContentResponse contentResponse = new ContentResponse(1L, "???????????? ???", "??????????????????", 2L,
            "https://img1.com", "https://okky.kr/articles/503803", 4,
            100, false, false, "2023-02-17T12:30.334", contentViewerRecommendationResponses);
        ContentResponse contentResponse2 = new ContentResponse(2L, "????????? ???????????? ??????????", "??????????????????", 2L,
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
    @DisplayName("[??????]")
    class Fail {

      @Test
      @DisplayName("category??? creatorId??? ????????? ???????????? CategoryAndCreatorIdConstraintViolationException??? ????????????.")
      void bothCategoryAndCreatorId() throws Exception {
        String category = "??????";
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
      @DisplayName("???????????? ?????? ???????????? ????????? ?????? ??????????????? ???????????? ????????? MissingServletRequestParameterException??? ????????????.")
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
  @DisplayName("[Admin] ????????? ????????? ?????? API???")
  class RetrieveInactivateContent {

    @BeforeEach
    void setUp() {
      authSetup();
    }

    @Nested
    @DisplayName("[??????]")
    class Success {

      @Test
      @DisplayName("???????????? ???????????? ????????????.")
      void retrieveInactiveContents() throws Exception {
        String page = "0";
        String size = "10";

        ContentAdminResponse content1 = new ContentAdminResponse(1L, "????????? ??????1",
            "https://d2.naver.com/aaa");
        ContentAdminResponse content2 = new ContentAdminResponse(2L, "????????? ??????2",
            "https://d2.naver.com/bbb");
        ContentAdminResponse content3 = new ContentAdminResponse(3L, "????????? ??????3",
            "https://d2.naver.com/ccc");
        ContentAdminResponse content4 = new ContentAdminResponse(4L, "????????? ??????4",
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
                            .description("????????? id"),
                        fieldWithPath("contents.[].title").type(JsonFieldType.STRING)
                            .description("????????? ??????"),
                        fieldWithPath("contents.[].link").type(JsonFieldType.STRING)
                            .description("????????? ?????? ?????? ??????"),
                        fieldWithPath("currentPage").type(JsonFieldType.NUMBER)
                            .description("?????? ????????? ??????"),
                        fieldWithPath("totalPage").type(JsonFieldType.NUMBER)
                            .description("?????? ????????? ??????")
                    )
                )
            );
      }
    }

  }

  @Nested
  @DisplayName("[Admin] ????????? ?????? API???")
  class DeleteContentByAdmin {

    @BeforeEach
    void setUp() {
      authSetup();
    }

    @Nested
    @DisplayName("[??????]")
    class Success {

      @Test
      @DisplayName("???????????? ????????????.")
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
