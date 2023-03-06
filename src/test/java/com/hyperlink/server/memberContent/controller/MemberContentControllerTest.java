package com.hyperlink.server.memberContent.controller;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperlink.server.AuthSetupForMock;
import com.hyperlink.server.domain.auth.token.JwtTokenProvider;
import com.hyperlink.server.domain.memberContent.application.MemberContentService;
import com.hyperlink.server.domain.memberContent.controller.MemberContentController;
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
import org.springframework.test.web.servlet.MockMvc;
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
  @MockBean
  JwtTokenProvider jwtTokenProvider;

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
}

