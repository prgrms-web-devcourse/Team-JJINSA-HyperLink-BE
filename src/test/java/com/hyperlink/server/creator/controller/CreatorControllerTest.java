package com.hyperlink.server.creator.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.domain.creator.application.CreatorService;
import com.hyperlink.server.domain.creator.controller.CreatorController;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollRequest;
import com.hyperlink.server.domain.creator.dto.CreatorEnrollResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

@WebMvcTest(CreatorController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class CreatorControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  CreatorService creatorService;

  @Nested
  @DisplayName("크리에이터 생성 API는")
  class EnrollCreatorTest {


    @Nested
    @DisplayName("[실패] 크리에이터 생성 요청 중")
    class Fail {

      @Test
      @DisplayName("크리에이터 이름이 누락되면 BadRequest를 응답한다.")
      public void blankInName() throws Exception {
        //given
        CreatorEnrollRequest creatorEnrollRequest = new CreatorEnrollRequest("", "profileImgUrl",
            "description", "categoryName");
        // when & then
        mockMvc.perform(
                post("/admin/creators")
                    .content(objectMapper.writeValueAsString(creatorEnrollRequest))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(response -> Assertions.assertTrue(
                response.getResolvedException() instanceof MethodArgumentNotValidException));
      }

      @Test
      @DisplayName("크리에이터 설명글(description)이 누락되면 BadRequest를 응답한다.")
      public void blankInDescription() throws Exception {
        //given
        CreatorEnrollRequest creatorEnrollRequest = new CreatorEnrollRequest("creatorName",
            "profileImgUrl",
            "", "categoryName");
        // when & then
        mockMvc.perform(
                post("/admin/creators")
                    .content(objectMapper.writeValueAsString(creatorEnrollRequest))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(response -> Assertions.assertTrue(
                response.getResolvedException() instanceof MethodArgumentNotValidException));
      }

      @Test
      @DisplayName("크리에이터 카테고리 이름이 누락되면 BadRequest를 응답한다.")
      public void blankInCategoryName() throws Exception {
        //given
        CreatorEnrollRequest creatorEnrollRequest = new CreatorEnrollRequest("creatorName",
            "profileImgUrl",
            "description", "");
        // when & then
        mockMvc.perform(
                post("/admin/creators")
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

      @Test
      @DisplayName("모든 요청 값이 올바르면 Created를 응답하고 CreatorEnrollResponse를 반환한다.")
      public void enrollCreatorStatusCreatedReturnCreatorEnrollResponse() throws Exception {
        //given
        CreatorEnrollRequest creatorEnrollRequest = new CreatorEnrollRequest("creatorName",
            "profileImgUrl", "description", "categoryName");
        given(creatorService.enrollCreator(creatorEnrollRequest)).willReturn(
            new CreatorEnrollResponse(1L, "creatorName", "profileImgUrl", "description",
                "categoryName"));
        // when & then
        mockMvc.perform(
                post("/admin/creators")
                    .content(objectMapper.writeValueAsString(creatorEnrollRequest))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andDo(print());
      }
    }
  }
}
