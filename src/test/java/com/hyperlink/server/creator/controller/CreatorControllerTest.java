package com.hyperlink.server.creator.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.AuthSetupForMock;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.creator.application.CreatorService;
import com.hyperlink.server.domain.creator.controller.CreatorController;
import com.hyperlink.server.domain.creator.domain.entity.Creator;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollRequest;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollResponse;
import com.hyperlink.server.domain.creator.exception.CreatorNotFoundException;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.notRecommendCreator.domain.entity.NotRecommendCreator;
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
import org.springframework.web.bind.MethodArgumentNotValidException;

@WebMvcTest(CreatorController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public class CreatorControllerTest extends AuthSetupForMock {

  @MockBean
  CreatorService creatorService;
  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;

  @Nested
  @DisplayName("크리에이터 생성 API는")
  class CreatorEnrollTest {


    @Nested
    @DisplayName("[실패] 크리에이터 생성 요청 중")
    class Fail {

      @BeforeEach
      void setUp() {
        authSetup();
      }

      @Test
      @DisplayName("크리에이터 이름이 누락되면 BadRequest를 응답한다.")
      public void blankInName() throws Exception {

        CreatorEnrollRequest creatorEnrollRequest = new CreatorEnrollRequest("", "profileImgUrl",
            "description", "categoryName");

        mockMvc.perform(
                post("/admin/creators")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .content(objectMapper.writeValueAsString(creatorEnrollRequest))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(response -> Assertions.assertTrue(
                response.getResolvedException() instanceof MethodArgumentNotValidException));
      }

      @Test
      @DisplayName("크리에이터 설명글(description)이 누락되면 BadRequest를 응답한다.")
      public void blankInDescription() throws Exception {

        CreatorEnrollRequest creatorEnrollRequest = new CreatorEnrollRequest("creatorName",
            "profileImgUrl",
            "", "categoryName");

        mockMvc.perform(
                post("/admin/creators")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .content(objectMapper.writeValueAsString(creatorEnrollRequest))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(response -> Assertions.assertTrue(
                response.getResolvedException() instanceof MethodArgumentNotValidException));
      }

      @Test
      @DisplayName("크리에이터 카테고리 이름이 누락되면 BadRequest를 응답한다.")
      public void blankInCategoryName() throws Exception {

        CreatorEnrollRequest creatorEnrollRequest = new CreatorEnrollRequest("creatorName",
            "profileImgUrl",
            "description", "");

        mockMvc.perform(
                post("/admin/creators")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .content(objectMapper.writeValueAsString(creatorEnrollRequest))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(response -> Assertions.assertTrue(
                response.getResolvedException() instanceof MethodArgumentNotValidException));
      }
    }

    @Nested
    @DisplayName("[성공] 크리에이터 생성 요청 중")
    class Success {

      @BeforeEach
      void setUp() {
        authSetup();
      }

      @Test
      @DisplayName("모든 요청 값이 올바르면 Created를 응답하고 CreatorEnrollResponse를 반환한다.")
      public void enrollCreatorStatusCreatedReturnCreatorEnrollResponse() throws Exception {

        CreatorEnrollRequest creatorEnrollRequest = new CreatorEnrollRequest("creatorName",
            "profileImgUrl", "description", "categoryName");
        given(creatorService.enrollCreator(creatorEnrollRequest)).willReturn(
            new CreatorEnrollResponse(1L, "creatorName", "profileImgUrl", "description",
                "categoryName"));

        mockMvc.perform(
                post("/admin/creators")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .content(objectMapper.writeValueAsString(creatorEnrollRequest))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andDo(print())
            .andDo(document("CreatorControllerTest/enrollCreator",
                requestHeaders(
                    headerWithName("Authorization").description("AccessToken")
                ),
                requestFields(
                    fieldWithPath("name").type(JsonFieldType.STRING).description("크리에이터 이름"),
                    fieldWithPath("profileImgUrl").type(JsonFieldType.STRING)
                        .description("크리에이터 프로필 이미지"),
                    fieldWithPath("description").type(JsonFieldType.STRING)
                        .description("크리에이터 소개글"),
                    fieldWithPath("categoryName").type(JsonFieldType.STRING)
                        .description("크리에이터의 카테고리 이름")
                ),
                responseFields(
                    fieldWithPath("creatorId").type(JsonFieldType.NUMBER).description("크리에이터 id"),
                    fieldWithPath("name").type(JsonFieldType.STRING).description("크리에이터 이름"),
                    fieldWithPath("profileImgUrl").type(JsonFieldType.STRING)
                        .description("크리에이터 프로필 이미지"),
                    fieldWithPath("description").type(JsonFieldType.STRING)
                        .description("크리에이터 소개글"),
                    fieldWithPath("categoryName").type(JsonFieldType.STRING)
                        .description("크리에이터의 카테고리 이름")
                )
            ));
      }
    }
  }

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
        Member member = new Member("email", "nickname", Career.ETC, CareerYear.EIGHT,
            "profileImgUrl");
        Category category = new Category("개발");
        Creator creator = new Creator("name", "profileImgUrl", "descriptions", category);
        NotRecommendCreator notRecommendCreator = new NotRecommendCreator(member, creator);

        doReturn(notRecommendCreator).when(creatorService).notRecommend(any(), any());

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

  @Nested
  @DisplayName("크리에이터 삭제 API는")
  class CreatorDeleteTest {

    @Nested
    @DisplayName("유효한 크리에이터 id가 들어오면")
    class Success {

      @BeforeEach
      void setUp() {
        authSetup();
      }

      @Test
      @DisplayName("해당 크리에이터를 삭제하고 OK를 응답한다")
      void deleteCreatorReturnsOK() throws Exception {
        Long creatorId = 1L;

        doNothing().when(creatorService).deleteCreator(creatorId);

        mockMvc.perform(delete("/admin/creators/" + creatorId)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(print())
            .andDo(document("CreatorControllerTest/deleteCreator",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName("Authorization").description("accessToken")
                )
            ));
      }
    }

    @Nested
    @DisplayName("유효하지 않은 크리에이터 id가 들어오면")
    class Fail {

      @BeforeEach
      void setUp() {
        authSetup();
      }

      @Test
      @DisplayName("해당 크리에이터를 삭제하지 않고 NOT FOUND를 응답한다")
      void deleteCreatorReturnNotFound() throws Exception {
        Long creatorId = 1L;
        doThrow(new CreatorNotFoundException()).when(creatorService).deleteCreator(creatorId);

        mockMvc.perform(delete("/admin/creators/" + creatorId)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(response -> Assertions.assertTrue(
                response.getResolvedException() instanceof CreatorNotFoundException))
            .andDo(print());
      }
    }
  }

}
