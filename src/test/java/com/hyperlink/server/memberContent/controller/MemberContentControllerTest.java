package com.hyperlink.server.memberContent.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.AuthSetupForMock;
import com.hyperlink.server.domain.content.application.ContentService;
import com.hyperlink.server.domain.content.dto.ContentResponse;
import com.hyperlink.server.domain.content.dto.ContentViewerRecommendationResponse;
import com.hyperlink.server.domain.memberContent.application.BookmarkService;
import com.hyperlink.server.domain.memberContent.application.LikeService;
import com.hyperlink.server.domain.memberContent.controller.MemberContentController;
import com.hyperlink.server.domain.memberContent.dto.BookmarkPageResponse;
import com.hyperlink.server.domain.memberContent.dto.LikeClickRequest;
import com.hyperlink.server.domain.memberContent.dto.LikeClickResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@WebMvcTest(MemberContentController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public class MemberContentControllerTest extends AuthSetupForMock {

  @MockBean
  BookmarkService bookmarkService;

  @MockBean
  LikeService likeService;

  @MockBean
  ContentService contentService;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Nested
  @DisplayName("북마크 추가/삭제 API는")
  class includeCreateOrDeleteBookmarkResponseTest {

    Long contentId = 1L;

    @Nested
    @DisplayName("type에 null 값이 들어오면")
    class Null {

      @Test
      @DisplayName("BadRequest를 응답한다")
      void failRequestTest() throws Exception {
        mockMvc.perform(
                post("/bookmark/" + contentId + "?type=")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            )
            .andExpect(status().isBadRequest())
            .andExpect(response -> Assertions.assertTrue(
                response.getResolvedException() instanceof MethodArgumentTypeMismatchException));
      }
    }

    @Nested
    @DisplayName("유효한 요청값이 들어오면")
    class Success {

      @Test
      @DisplayName("북마크 삭제에 성공하고 OK를 응답한다")
      void deleteBookmarkTest() throws Exception {
        authSetup();

        mockMvc.perform(
                post("/bookmark/" + contentId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .param("type", "0")
            )
            .andExpect(status().isOk())
            .andDo(
                document(
                    "MemberContentController/includeCreateOrDeleteBookmark",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("jwt header")
                    )
                )
            );
      }

      @Test
      @DisplayName("북마크 추가에 성공하고 OK를 응답한다")
      void createBookmarkTest() throws Exception {
        authSetup();

        mockMvc.perform(
                post("/bookmark/" + contentId)
                    .param("type", "1")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            )
            .andExpect(status().isOk())
            .andDo(
                document(
                    "MemberContentController/includeCreateOrDeleteBookmark",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("jwt header")
                    )
                )
            );
      }
    }
  }

  @Test
  void getBookmarkPage() throws Exception {
    List<ContentResponse> contents = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      ContentViewerRecommendationResponse recommendationCompanyResponse = new ContentViewerRecommendationResponse(
          "bannserName" + i, "bannerLogoUrl" + i);
      contents.add(
          new ContentResponse(Long.valueOf(i), "title" + i, "creatorName" + i,
              Long.valueOf(i), "contentUrl" + i, "linkUrl" + i, i, i, true, false,
              LocalDateTime.now().toString(), Arrays.asList(recommendationCompanyResponse)));
    }

    BookmarkPageResponse bookmarkPageResponse = new BookmarkPageResponse(contents, true);

    given(bookmarkService.findBookmarkedContentForSlice(memberId, 0, 2))
        .willReturn(bookmarkPageResponse);

    authSetup();

    mockMvc.perform(
            MockMvcRequestBuilders.get("/bookmark")
                .param("page", "0")
                .param("size", "2")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
        )
        .andExpect(status().isOk())
        .andDo(
            document(
                "MemberContentController/getBookmarks",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName(HttpHeaders.AUTHORIZATION).description("jwt header")),
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
                        JsonFieldType.ARRAY).description("회사 추천 배열"),
                    fieldWithPath("contents.[].recommendations.[].bannerName").type(
                        JsonFieldType.STRING).description("회사명"),
                    fieldWithPath(
                        "contents.[].recommendations.[].bannerLogoImgUrl").type(
                        JsonFieldType.STRING).description("회사 로고 URL"),
                    fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
                        .description("다음 페이지 존재여부"))));
  }

  @Nested
  @DisplayName("좋아요 클릭 API는")
  class clickLikeTest {

    Long contentId = 1L;
    LikeClickRequest likeClickRequestForCreate = new LikeClickRequest(true);
    LikeClickRequest likeClickRequestForDelete = new LikeClickRequest(false);


    @Test
    @DisplayName("좋아요 추가에 성공하고 OK를 응답한다")
    void createLikeTest() throws Exception {
      authSetup();

      Long contentId = 1L;

      LikeClickResponse likeClickResponse = new LikeClickResponse(10);

      given(likeService.clickLike(memberId, contentId, likeClickRequestForCreate)).willReturn(
          likeClickResponse);

      mockMvc.perform(
              post("/like/" + contentId)
                  .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(likeClickRequestForCreate)))
          .andExpect(status().isOk())
          .andDo(
              document(
                  "MemberContentController/clickLike",
                  preprocessRequest(prettyPrint()),
                  preprocessResponse(prettyPrint()),
                  requestHeaders(
                      headerWithName(HttpHeaders.AUTHORIZATION).description("jwt header")
                  ), requestFields(
                      fieldWithPath("addLike").type(JsonFieldType.BOOLEAN)
                          .description("좋아요 클릭 요청 타입(true: 좋아요추가, false: 좋아요 취소)")),
                  responseFields(
                      fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("좋아요 수"))
              )
          );
    }

    @Test
    @DisplayName("좋아요 취소에 성공하고 OK를 응답한다")
    void deleteLikeTest() throws Exception {
      authSetup();
      Long contentId = 1L;

      LikeClickResponse likeClickResponse = new LikeClickResponse(10);

      given(likeService.clickLike(memberId, contentId, likeClickRequestForDelete)).willReturn(
          likeClickResponse);

      mockMvc.perform(
              post("/like/" + contentId)
                  .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(likeClickRequestForDelete)))
          .andExpect(status().isOk())
          .andDo(
              document(
                  "MemberContentController/clickLike",
                  preprocessRequest(prettyPrint()),
                  preprocessResponse(prettyPrint()),
                  requestHeaders(
                      headerWithName(HttpHeaders.AUTHORIZATION).description("jwt header")
                  ), requestFields(
                      fieldWithPath("addLike").type(JsonFieldType.BOOLEAN)
                          .description("좋아요 클릭 요청 타입(true: 좋아요추가, false: 좋아요 취소)")),
                  responseFields(
                      fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("좋아요 수"))
              )
          );
    }
  }
}

