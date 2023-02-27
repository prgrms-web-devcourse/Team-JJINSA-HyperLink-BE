package com.hyperlink.server.creator.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.creator.application.CreatorService;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.memberCreator.domain.entity.MemberCreator;
import javax.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@Transactional
public class CreatorControllerTest {

  @MockBean
  CreatorService creatorService;
  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;

  @Nested
  @DisplayName("크리에이터 비추천 API는")
  class NotRecommend {

    @Nested
    @DisplayName("유효한 요청값이 들어오면")
    class Success {

      @Test
      @DisplayName("해당 멤버의 비추천 크리에이터 목록에 추가하고 OK를 응답한다")
      void addNotRecommend() throws Exception {
        long creatorId = 10L;
        Member member = new Member("email", "nickname", "career", "careerYear", "profileImgUrl");
        Category category = new Category("개발");
        Creator creator = new Creator("name", "profileImgUrl", "descriptions", category);
        MemberCreator memberCreator = new MemberCreator(member, creator);

        doReturn(memberCreator).when(creatorService).notRecommend(any(), any());

        mockMvc.perform(
                post("/creators/" + creatorId + "/not-recommend")
                    // .header("AccessToken", accessToken)
                    .characterEncoding("UTF-8")
            )
            .andExpect(status().isOk())
            .andDo(
                document(
                    "CreatorControllerTest/notRecommend",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    requestHeaders(
                        // TODO : jwt
//                        headerWithName("AccessToken").description("jwt header")
                    )
                )
            );
      }
    }
  }

}
