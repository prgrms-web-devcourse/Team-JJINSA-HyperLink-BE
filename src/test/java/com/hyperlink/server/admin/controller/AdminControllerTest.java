package com.hyperlink.server.admin.controller;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hyperlink.server.AdminAuthSetupForMock;
import com.hyperlink.server.domain.admin.application.AdminService;
import com.hyperlink.server.domain.admin.controller.AdminController;
import com.hyperlink.server.domain.admin.domain.vo.CategoryAndView;
import com.hyperlink.server.domain.admin.dto.CategoryViewResponse;
import com.hyperlink.server.domain.admin.dto.CategoryViewResponses;
import java.util.List;
import java.util.Optional;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public class AdminControllerTest extends AdminAuthSetupForMock {

  @MockBean
  AdminService adminService;
  @Autowired
  MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    authSetup();
  }

  @Nested
  @DisplayName("카테고리 별 조회수 조회 API는")
  class CountViewByCategoryTest {

    @Test
    @DisplayName("조회일로부터 7일 이전의 카테고리별 조회수를 조회한다.")
    void countViewsByCategoryAndDate() throws Exception {
      CategoryAndView develop = new CategoryAndView("develop", 133);
      CategoryAndView beauty = new CategoryAndView("beauty", 152);
      CategoryViewResponse categoryViewResponse1 = new CategoryViewResponse(
          List.of(develop, beauty),
          "2023-03-03");
      CategoryViewResponse categoryViewResponse2 = new CategoryViewResponse(
          List.of(develop, beauty),
          "2023-03-04");
      CategoryViewResponse categoryViewResponse3 = new CategoryViewResponse(
          List.of(develop, beauty),
          "2023-03-05");
      CategoryViewResponse categoryViewResponse4 = new CategoryViewResponse(
          List.of(develop, beauty),
          "2023-03-06");
      CategoryViewResponse categoryViewResponse5 = new CategoryViewResponse(
          List.of(develop, beauty),
          "2023-03-07");
      CategoryViewResponse categoryViewResponse6 = new CategoryViewResponse(
          List.of(develop, beauty),
          "2023-03-08");
      CategoryViewResponse categoryViewResponse7 = new CategoryViewResponse(
          List.of(develop, beauty),
          "2023-03-09");
      CategoryViewResponses categoryViewResponses = new CategoryViewResponses(
          List.of(categoryViewResponse1, categoryViewResponse2, categoryViewResponse3,
              categoryViewResponse4, categoryViewResponse5, categoryViewResponse6,
              categoryViewResponse7), "2023-03-10");
      when(adminService.countViewCountByCategory()).thenReturn(categoryViewResponses);
      when(adminService.getCategoryView()).thenReturn(Optional.of(categoryViewResponses));

      mockMvc.perform(get("/admin/dashboard/all-category/view")
              .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
          )
          .andExpect(status().isOk())
          .andDo(print())
          .andDo(
              document("AdminController/countViewCountByCategory",
                  preprocessRequest(prettyPrint()),
                  preprocessResponse(prettyPrint()),
                  requestHeaders(
                      headerWithName("Authorization").description("accessToken")
                  ),
                  responseFields(
                      fieldWithPath("weeklyViewCounts[]").type(JsonFieldType.ARRAY)
                          .description("7일 간의 조회수 조회 배열"),
                      fieldWithPath("weeklyViewCounts[].results[]").type(JsonFieldType.ARRAY)
                          .description("1일 간의 카테고리별 조회수 정보 배열"),
                      fieldWithPath("weeklyViewCounts[].results[].categoryName").type(
                          JsonFieldType.STRING).description("카테고리 이름"),
                      fieldWithPath("weeklyViewCounts[].results[].viewCount").type(
                          JsonFieldType.NUMBER).description("조회수"),
                      fieldWithPath("weeklyViewCounts[].date").type(JsonFieldType.STRING)
                          .description("조회 대상 날짜"),
                      fieldWithPath("createdDate").type(JsonFieldType.STRING)
                          .description("데이터 생성 날짜")
                  )
              )
          );
    }
  }

}
