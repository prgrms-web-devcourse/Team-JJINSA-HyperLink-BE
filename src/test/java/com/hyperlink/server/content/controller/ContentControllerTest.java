package com.hyperlink.server.content.controller;

import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.AuthSetupForMock;
import com.hyperlink.server.domain.auth.token.JwtTokenProvider;
import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.content.controller.ContentController;
import com.hyperlink.server.domain.content.dto.ContentResponse;
import com.hyperlink.server.domain.content.dto.GetContentsCommonResponse;
import com.hyperlink.server.domain.content.dto.RecommendationCompanyResponse;
import com.hyperlink.server.domain.content.dto.SearchResponse;
import java.util.List;
import javax.validation.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ContentController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public class ContentControllerTest extends AuthSetupForMock {

  @MockBean
  ContentService contentService;
  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;
  @MockBean
  JwtTokenProvider jwtTokenProvider;

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

        List<RecommendationCompanyResponse> recommendationCompanyResponses = List.of(
            new RecommendationCompanyResponse("네이버", "https://imglogo.com"));
        ContentResponse contentResponse = new ContentResponse(27L, "개발자의 삶", "슈카", 2L,
            "https://img1.com", "https://okky.kr/articles/503803", 4,
            100, false, false, "2023-02-17T12:30.334", recommendationCompanyResponses);
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
                        fieldWithPath("getContentsCommonResponse.contents.[].contentId").type(JsonFieldType.NUMBER)
                            .description("컨텐츠 id"),
                        fieldWithPath("getContentsCommonResponse.contents.[].title").type(JsonFieldType.STRING)
                            .description("컨텐츠 제목"),
                        fieldWithPath("getContentsCommonResponse.contents.[].creatorName").type(JsonFieldType.STRING)
                            .description("크리에이터 이름"),
                        fieldWithPath("getContentsCommonResponse.contents.[].creatorId").type(JsonFieldType.NUMBER)
                            .description("크리에이터 id"),
                        fieldWithPath("getContentsCommonResponse.contents.[].contentImgUrl").type(JsonFieldType.STRING)
                            .description("컨텐츠 이미지 URL"),
                        fieldWithPath("getContentsCommonResponse.contents.[].link").type(JsonFieldType.STRING)
                            .description("컨텐츠 연결 외부 링크"),
                        fieldWithPath("getContentsCommonResponse.contents.[].likeCount").type(JsonFieldType.NUMBER)
                            .description("좋아요 개수"),
                        fieldWithPath("getContentsCommonResponse.contents.[].viewCount").type(JsonFieldType.NUMBER)
                            .description("조회수 개수"),
                        fieldWithPath("getContentsCommonResponse.contents.[].isBookmarked").type(JsonFieldType.BOOLEAN)
                            .description("북마크 여부"),
                        fieldWithPath("getContentsCommonResponse.contents.[].isLiked").type(JsonFieldType.BOOLEAN)
                            .description("좋아요 여부"),
                        fieldWithPath("getContentsCommonResponse.contents.[].createdAt").type(JsonFieldType.STRING)
                            .description("컨텐츠 생성 날짜"),
                        fieldWithPath("getContentsCommonResponse.contents.[].recommendationCompanies").type(
                            JsonFieldType.ARRAY).description("회사 추천 배열"),
                        fieldWithPath("getContentsCommonResponse.contents.[].recommendationCompanies.[].companyName").type(
                            JsonFieldType.STRING).description("회사명"),
                        fieldWithPath(
                            "getContentsCommonResponse.contents.[].recommendationCompanies.[].companyLogoImgUrl").type(
                            JsonFieldType.STRING).description("회사 로고 URL"),
                        fieldWithPath("getContentsCommonResponse.hasNext").type(JsonFieldType.BOOLEAN)
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

}
