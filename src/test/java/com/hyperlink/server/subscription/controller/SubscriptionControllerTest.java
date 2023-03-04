package com.hyperlink.server.subscription.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.AuthSetupForMock;
import com.hyperlink.server.domain.creator.exception.CreatorNotFoundException;
import com.hyperlink.server.domain.member.exception.MemberNotFoundException;
import com.hyperlink.server.domain.subscription.application.SubscriptionService;
import com.hyperlink.server.domain.subscription.controller.SubscriptionController;
import com.hyperlink.server.domain.subscription.dto.SubscribeResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

@WebMvcTest(SubscriptionController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public class SubscriptionControllerTest extends AuthSetupForMock {

  @MockBean
  SubscriptionService subscriptionService;

  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;

  @Nested
  @DisplayName("크리에이터 구독 추가/삭제 API는")
  class SubscribeCreatorTest {

    @Nested
    @DisplayName("[성공]")
    class Success {

      @Nested
      @DisplayName("로그인 한")
      class IfLogin {

        @BeforeEach
        void setUp() {
          authSetup();
        }

        @Test
        @DisplayName("사용자가 크리에이터를 이미 구독한 상황이었다면 구독 취소하고 200 OK와 구독 상태를 반환한다.")
        void unsubscribeIfSubscribed() throws Exception {
          Long loginMemberId = 1L;
          Long creatorId = 1L;

          SubscribeResponse subscribeResponse = new SubscribeResponse(false);
          given(subscriptionService.subscribeOrUnsubscribeCreator(loginMemberId, creatorId))
              .willReturn(subscribeResponse);

          mockMvc.perform(
                  post("/creators/" + creatorId + "/subscribe")
                      .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                      .characterEncoding("UTF-8"))
              .andExpect(status().isOk())
              .andDo(print())
              .andDo(document("SubscriptionControllerTest/subscribeOrUnsubscribeCreator",
                  preprocessRequest(prettyPrint()),
                  preprocessResponse(prettyPrint()),
                  requestHeaders(
                      headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")
                  ),
                  responseFields(
                      fieldWithPath("isSubscribed").type(JsonFieldType.BOOLEAN)
                          .description("구독 여부 상태")
                  )
              ));
        }

        @Test
        @DisplayName("사용자가 크리에이터를 구독하지 않은 상황이었다면 구독하고 200 OK와 구독 상태를 반환한다.")
        void subscribeIfUnsubscribed() throws Exception {
          Long loginMemberId = 1L;
          Long creatorId = 1L;

          SubscribeResponse subscribeResponse = new SubscribeResponse(true);
          when(subscriptionService.subscribeOrUnsubscribeCreator(loginMemberId, creatorId))
              .thenReturn(subscribeResponse);

          mockMvc.perform(post("/creators/" + creatorId + "/subscribe")
                  .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                  .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andDo(print());
        }
      }
    }


    @Nested
    @DisplayName("[실패]")
    class Fail {

      @Nested
      @DisplayName("잘못된")
      class IfLogin {

        @BeforeEach
        void setUp() {
          authSetup();
        }

        @Test
        @DisplayName("사용자가 크리에이터를 크리에이터에 대해 구독버튼을 누르면 404 NOT FOUND 를  반환한다.")
        void throwsMemberNotFoundException() throws Exception {
          Long wrongMemberId = 1L;
          Long creatorId = 1L;

          when(subscriptionService.subscribeOrUnsubscribeCreator(wrongMemberId, creatorId))
              .thenThrow(new MemberNotFoundException());

          mockMvc.perform(
                  post("/creators/" + creatorId + "/subscribe")
                      .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                      .characterEncoding("UTF-8"))
              .andExpect(status().isNotFound())
              .andDo(print())
              .andExpect(response -> Assertions.assertTrue(
                      response.getResolvedException() instanceof MemberNotFoundException));
        }

        @Test
        @DisplayName("크리에이터에 대해 구독버튼을 누르면 404 NOT FOUND 를 반환한다.")
        void throwsCreatorNotFoundException() throws Exception {
          Long loginMemberId = 1L;
          Long wrongCreatorId = 1L;

          when(subscriptionService.subscribeOrUnsubscribeCreator(loginMemberId, wrongCreatorId))
              .thenThrow(new CreatorNotFoundException());

          mockMvc.perform(
                  post("/creators/" + wrongCreatorId + "/subscribe")
                      .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                      .characterEncoding("UTF-8"))
              .andExpect(status().isNotFound())
              .andDo(print())
              .andExpect(response -> Assertions.assertTrue(
                  response.getResolvedException() instanceof CreatorNotFoundException));
        }
      }

      @Nested
      @DisplayName("로그인 하지 않은")
      class IfNotLogin {

        @Test
        @DisplayName("사용자가 구독 버튼을 누르면 401 UnAuthorized 를 반환한다.")
        void unAuthorized() {

        }
      }


    }
  }

}
