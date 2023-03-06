package com.hyperlink.server.memberHistory.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperlink.server.AuthSetupForMock;
import com.hyperlink.server.domain.content.dto.ContentResponse;
import com.hyperlink.server.domain.content.dto.GetContentsCommonResponse;
import com.hyperlink.server.domain.content.dto.RecommendationCompanyResponse;
import com.hyperlink.server.domain.memberHistory.application.MemberHistoryService;
import com.hyperlink.server.domain.memberHistory.controller.MemberHistoryController;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@WebMvcTest(MemberHistoryController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public class MemberHistoryControllerTest extends AuthSetupForMock {

  @MockBean
  MemberHistoryService memberHistoryService;

  @Autowired
  MockMvc mockMvc;

  @Nested
  @DisplayName("히스토리 전체 조회 API는")
  class GetAllHistory {

    @Nested
    @DisplayName("page 혹은 size에 null 값이 들어오면")
    class Null {

      @ParameterizedTest
      @NullSource
      @DisplayName("BadRequest를 응답한다")
      void failRequestTest(Integer nullValue) throws Exception {
        authSetup();

        mockMvc.perform(
                get("/history")
                    .param("page", String.valueOf(nullValue))
                    .param("size", String.valueOf(nullValue))
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            )
            .andExpect(status().isBadRequest())
            .andExpect(response -> assertTrue(
                response.getResolvedException() instanceof MethodArgumentTypeMismatchException));
      }
    }

    @Nested
    @DisplayName("유효한 요청값이 들어오면")
    class Success {

      String page = "0", size = "10";

      @Test
      @DisplayName("OK와 유저의 히스토리 전체 내역을 응답한다")
      void addInquiryOfContentTest() throws Exception {
        authSetup();

        List<RecommendationCompanyResponse> recommendationCompanyResponses = List.of(
            new RecommendationCompanyResponse("네이버", "https://imglogo.com"));
        ContentResponse contentResponse = new ContentResponse(27L, "개발자의 삶", "슈카", 2L,
            "https://img1.com", "https://okky.kr/articles/503803", 4,
            100, false, false, "2023-02-17T12:30.334", recommendationCompanyResponses);
        List<ContentResponse> contentResponses = List.of(contentResponse);
        GetContentsCommonResponse getContentsCommonResponse = new GetContentsCommonResponse(
            contentResponses, true);

        doReturn(getContentsCommonResponse).when(memberHistoryService).getAllHistory(any(), any());

        mockMvc.perform(
                get("/history")
                    .param("page", page)
                    .param("size", size)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            ).andExpect(status().isOk())
            .andDo(
                document(
                    "MemberHistoryControllerTest/getAllHistory",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("jwt header")
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
                        fieldWithPath("contents.[].recommendationCompanies").type(
                            JsonFieldType.ARRAY).description("회사 추천 배열"),
                        fieldWithPath("contents.[].recommendationCompanies.[].companyName").type(
                            JsonFieldType.STRING).description("회사명"),
                        fieldWithPath(
                            "contents.[].recommendationCompanies.[].companyLogoImgUrl").type(
                            JsonFieldType.STRING).description("회사 로고 URL"),
                        fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
                            .description("다음 페이지 존재여부")
                    )
                )
            );
      }
    }
  }

}
