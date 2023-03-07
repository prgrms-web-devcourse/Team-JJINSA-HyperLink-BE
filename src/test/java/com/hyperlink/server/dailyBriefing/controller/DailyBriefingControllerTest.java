package com.hyperlink.server.dailyBriefing.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.AuthSetupForMock;
import com.hyperlink.server.domain.dailyBriefing.application.DailyBriefingService;
import com.hyperlink.server.domain.dailyBriefing.controller.DailyBriefingController;
import com.hyperlink.server.domain.dailyBriefing.dto.DailyBriefing;
import com.hyperlink.server.domain.dailyBriefing.dto.GetDailyBriefingResponse;
import com.hyperlink.server.domain.dailyBriefing.dto.StatisticsByCategoryResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DailyBriefingController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public class DailyBriefingControllerTest extends AuthSetupForMock {

  @MockBean
  DailyBriefingService dailyBriefingService;
  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;

  @Nested
  @DisplayName("데일리브리핑 조회 API는")
  class GetDailyBriefing {

    @Nested
    @DisplayName("유효한 요청값이 들어오면")
    class Success {
      GetDailyBriefingResponse getDailyBriefingResponse;

      @BeforeEach
      void setUp() {
        LocalDateTime standardTime = LocalDateTime.now();
        List<StatisticsByCategoryResponse> viewByCategories = List.of(
            new StatisticsByCategoryResponse("develop", 283, 3),
            new StatisticsByCategoryResponse("beauty", 832, 1),
            new StatisticsByCategoryResponse("finance", 425, 2));
        List<StatisticsByCategoryResponse> memberCountByAttentionCategories = List.of(
            new StatisticsByCategoryResponse("develop", 13, 3),
            new StatisticsByCategoryResponse("beauty", 92, 1),
            new StatisticsByCategoryResponse("finance", 55, 2));

        DailyBriefing dailyBriefing = new DailyBriefing(300, 1540, viewByCategories,
            45, memberCountByAttentionCategories);
        getDailyBriefingResponse = new GetDailyBriefingResponse(
            standardTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), dailyBriefing);
      }

      @Test
      @DisplayName("데일리브리핑 조회에 성공하고 OK와 데일리브리핑을 응답한다")
      void getDailyBriefing() throws Exception {
        doReturn(getDailyBriefingResponse).when(dailyBriefingService).getDailyBriefing(any());

        mockMvc.perform(
                get("/daily-briefing")
            )
            .andExpect(status().isOk())
            .andDo(
                document(
                    "DailyBriefingControllerTest/getDailyBriefing",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("standardTime").type(JsonFieldType.STRING)
                            .description("통계 기준 시간"),
                        fieldWithPath("dailyBriefing").type(JsonFieldType.OBJECT)
                            .description("데일리브리핑 데이터"),
                        fieldWithPath("dailyBriefing.memberIncrease").type(JsonFieldType.NUMBER)
                            .description("24시간 동안 증가된 가입회원 수"),
                        fieldWithPath("dailyBriefing.viewIncrease").type(JsonFieldType.NUMBER)
                            .description("24시간 동안 집계된 총 조회수"),
                        fieldWithPath("dailyBriefing.viewByCategories").type(JsonFieldType.ARRAY)
                            .description("24시간 동안 집계된 카테고리별 조회수"),
                        fieldWithPath("dailyBriefing.viewByCategories.[].categoryName").type(
                                JsonFieldType.STRING)
                            .description("카테고리 이름"),
                        fieldWithPath("dailyBriefing.viewByCategories.[].count").type(
                                JsonFieldType.NUMBER)
                            .description("24시간 동안 집계된 조회수"),
                        fieldWithPath("dailyBriefing.viewByCategories.[].ranking").type(
                                JsonFieldType.NUMBER)
                            .description("24시간 동안 집계된 조회수 기준 카테고리 랭킹"),
                        fieldWithPath("dailyBriefing.contentIncrease").type(JsonFieldType.NUMBER)
                            .description("오늘 새로 추가된 컨텐츠 수"),
                        fieldWithPath("dailyBriefing.memberCountByAttentionCategories").type(
                                JsonFieldType.ARRAY)
                            .description("관심 카테고리별 고객 수"),
                        fieldWithPath(
                            "dailyBriefing.memberCountByAttentionCategories.[].categoryName").type(
                                JsonFieldType.STRING)
                            .description("카테고리 이름"),
                        fieldWithPath(
                            "dailyBriefing.memberCountByAttentionCategories.[].count").type(
                                JsonFieldType.NUMBER)
                            .description("해당 카테고리를 관심 카테고리로 설정한 고객 수"),
                        fieldWithPath(
                            "dailyBriefing.memberCountByAttentionCategories.[].ranking").type(
                                JsonFieldType.NUMBER)
                            .description("관심 카테고리별 회원 수 기준 카테고리 랭킹")
                    )
                )
            );
      }
    }
  }
}
