package com.hyperlink.server.attentionCategory.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.AuthSetupForMock;
import com.hyperlink.server.domain.attentionCategory.application.AttentionCategoryService;
import com.hyperlink.server.domain.attentionCategory.controller.AttentionCategoryController;
import com.hyperlink.server.domain.attentionCategory.dto.AttentionCategoryRequest;
import com.hyperlink.server.domain.attentionCategory.dto.AttentionCategoryResponse;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureRestDocs
@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(controllers = AttentionCategoryController.class)
class AttentionCategoryControllerTest extends AuthSetupForMock {

  @MockBean
  private AttentionCategoryService attentionCategoryService;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Test
  void changeAttentionCategoryTest() throws Exception {
    authSetup();

    List<String> nameList = Arrays.asList("develop", "beauty");
    AttentionCategoryRequest attentionCategoryRequest = new AttentionCategoryRequest(nameList);
    AttentionCategoryResponse attentionCategoryResponse = new AttentionCategoryResponse(nameList);
    given(attentionCategoryService.changeAttentionCategory(memberId,
        attentionCategoryRequest.attentionCategory()))
        .willReturn(attentionCategoryResponse);

    mockMvc.perform(MockMvcRequestBuilders
            .put("/attention-category/update")
            .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(attentionCategoryRequest)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andDo(document("Attention-category/update",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")),
            responseFields(
                fieldWithPath("attentionCategory").type(JsonFieldType.ARRAY)
                    .description("관심 카테고리 목록"))));
  }

  @Test
  void getAttentionCategoryTest() throws Exception {
    authSetup();

    List<String> nameList = Arrays.asList("develop", "beauty");
    AttentionCategoryResponse attentionCategoryResponse = new AttentionCategoryResponse(nameList);
    given(attentionCategoryService.getAttentionCategory(memberId))
        .willReturn(attentionCategoryResponse);

    mockMvc.perform(MockMvcRequestBuilders
            .get("/attention-category")
            .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andDo(document("Attention-category/get",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")),
            responseFields(
                fieldWithPath("attentionCategory").type(JsonFieldType.ARRAY)
                    .description("관심 카테고리 이름 목록"))));
  }

}