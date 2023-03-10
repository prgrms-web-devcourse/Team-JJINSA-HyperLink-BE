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
import com.hyperlink.server.domain.content.dto.ContentViewerRecommendationResponse;
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
  @DisplayName("???????????? ?????? ?????? API???")
  class GetAllHistory {

    @Nested
    @DisplayName("page ?????? size??? null ?????? ????????????")
    class Null {

      @ParameterizedTest
      @NullSource
      @DisplayName("BadRequest??? ????????????")
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
    @DisplayName("????????? ???????????? ????????????")
    class Success {

      String page = "0", size = "10";

      @Test
      @DisplayName("OK??? ????????? ???????????? ?????? ????????? ????????????")
      void addInquiryOfContentTest() throws Exception {
        authSetup();

        List<ContentViewerRecommendationResponse> contentViewerRecommendationResponses = List.of(
            new ContentViewerRecommendationResponse("?????????", "https://imglogo.com"));
        ContentResponse contentResponse = new ContentResponse(27L, "???????????? ???", "??????", 2L,
            "https://img1.com", "https://okky.kr/articles/503803", 4,
            100, false, false, "2023-02-17T12:30.334", contentViewerRecommendationResponses);
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
                            JsonFieldType.ARRAY).description("?????? ?????? ??????"),
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
  }

}
