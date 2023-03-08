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
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperlink.server.AuthSetupForMock;
import com.hyperlink.server.domain.content.dto.BookMarkedContentPageResponse;
import com.hyperlink.server.domain.memberContent.application.MemberContentService;
import com.hyperlink.server.domain.memberContent.controller.MemberContentController;
import com.hyperlink.server.domain.memberContent.dto.BookmarkPageResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
  MemberContentService memberContentService;
  @Autowired
  MockMvc mockMvc;

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
    List<BookMarkedContentPageResponse> contents = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      contents.add(
          new BookMarkedContentPageResponse(Long.valueOf(i), "title" + i, "contentImgUrl" + i,
              "link" + i,
              i, i, LocalDateTime.now()));
    }

    BookmarkPageResponse bookmarkPageResponse = new BookmarkPageResponse(contents, true);

    given(memberContentService.findBookmarkedContentForSlice(memberId, 0, 2))
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
                        .description("컨텐츠 식별자"),
                    fieldWithPath("contents.[].title").type(JsonFieldType.STRING)
                        .description("컨텐츠 제목"),
                    fieldWithPath("contents.[].contentImgUrl").type(JsonFieldType.STRING)
                        .description("컨텐츠 이미지 URL"),
                    fieldWithPath("contents.[].link").type(JsonFieldType.STRING)
                        .description("컨텐츠 바로가기 링크"),
                    fieldWithPath("contents.[].likeCount").type(JsonFieldType.NUMBER)
                        .description("컨텐츠 좋아요 수"),
                    fieldWithPath("contents.[].viewCount").type(JsonFieldType.NUMBER)
                        .description("컨텐츠 조회수"),
                    fieldWithPath("contents.[].createdAt").type(JsonFieldType.STRING)
                        .description("서비스 등록 날짜"),
                    fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
                        .description("다음 페이지 여부"))));
  }
}

