package com.hyperlink.server.content.controller;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.domain.content.application.ContentService;
import javax.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Transactional
public class ContentControllerTest {

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
            patch("/contents/"+ contentId +"/view")
        )
        .andExpect(status().isOk())
        .andDo(
            document(
                "ContentControllerTest/addInquiryOfContent",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                    fieldWithPath("viewCount").type(JsonFieldType.NUMBER)
                        .description("조회수 추가 완료 후 최종 조회수")
                )
            )
        );
      }
    }
  }

}
