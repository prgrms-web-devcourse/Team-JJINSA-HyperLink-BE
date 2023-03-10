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
import com.hyperlink.server.domain.dailyBriefing.domain.vo.ContentStatistics;
import com.hyperlink.server.domain.dailyBriefing.domain.vo.DailyBriefing;
import com.hyperlink.server.domain.dailyBriefing.domain.vo.MemberStatistics;
import com.hyperlink.server.domain.dailyBriefing.domain.vo.ViewStatistics;
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
  @DisplayName("?????????????????? ?????? API???")
  class GetDailyBriefing {

    @Nested
    @DisplayName("????????? ???????????? ????????????")
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
        List<ContentStatistics> contentIncreaseForWeek = List.of(
            new ContentStatistics("2023-03-01", 44),
            new ContentStatistics("2023-03-02", 23),
            new ContentStatistics("2023-03-03", 63),
            new ContentStatistics("2023-03-04", 29),
            new ContentStatistics("2023-03-05", 16),
            new ContentStatistics("2023-03-06", 45),
            new ContentStatistics("2023-03-07", 55)
        );

        DailyBriefing dailyBriefing = new DailyBriefing(new MemberStatistics(300, 1938), new ViewStatistics(1540, 28304), viewByCategories,
            contentIncreaseForWeek, memberCountByAttentionCategories);
        getDailyBriefingResponse = new GetDailyBriefingResponse(
            standardTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), dailyBriefing);
      }

      @Test
      @DisplayName("?????????????????? ????????? ???????????? OK??? ????????????????????? ????????????")
      void getDailyBriefing() throws Exception {
        doReturn(getDailyBriefingResponse).when(dailyBriefingService).getDailyBriefingResponse(any());

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
                            .description("?????? ?????? ??????"),
                        fieldWithPath("dailyBriefing").type(JsonFieldType.OBJECT)
                            .description("?????????????????? ?????????"),
                        fieldWithPath("dailyBriefing.memberStatistics.increase").type(JsonFieldType.NUMBER)
                            .description("24?????? ?????? ????????? ???????????? ???"),
                        fieldWithPath("dailyBriefing.memberStatistics.totalCount").type(JsonFieldType.NUMBER)
                            .description("?????? ???????????? ???"),
                        fieldWithPath("dailyBriefing.viewStatistics.increase").type(JsonFieldType.NUMBER)
                            .description("24?????? ?????? ????????? ??? ?????????"),
                        fieldWithPath("dailyBriefing.viewStatistics.totalCount").type(JsonFieldType.NUMBER)
                            .description("?????? ?????????"),
                        fieldWithPath("dailyBriefing.viewByCategories").type(JsonFieldType.ARRAY)
                            .description("24?????? ?????? ????????? ??????????????? ?????????"),
                        fieldWithPath("dailyBriefing.viewByCategories.[].categoryName").type(
                                JsonFieldType.STRING)
                            .description("???????????? ??????"),
                        fieldWithPath("dailyBriefing.viewByCategories.[].count").type(
                                JsonFieldType.NUMBER)
                            .description("24?????? ?????? ????????? ?????????"),
                        fieldWithPath("dailyBriefing.viewByCategories.[].ranking").type(
                                JsonFieldType.NUMBER)
                            .description("24?????? ?????? ????????? ????????? ?????? ???????????? ??????"),
                        fieldWithPath("dailyBriefing.contentIncreaseForWeek").type(
                                JsonFieldType.ARRAY)
                            .description("7??? ?????? ????????? ?????? ????????? ????????? ???"),
                        fieldWithPath("dailyBriefing.contentIncreaseForWeek.[].date").type(
                                JsonFieldType.STRING)
                            .description("?????? ??????"),
                        fieldWithPath("dailyBriefing.contentIncreaseForWeek.[].contentIncrease").type(
                                JsonFieldType.NUMBER)
                            .description("?????? ????????? ????????? ???"),
                        fieldWithPath("dailyBriefing.memberCountByAttentionCategories").type(
                                JsonFieldType.ARRAY)
                            .description("?????? ??????????????? ?????? ???"),
                        fieldWithPath(
                            "dailyBriefing.memberCountByAttentionCategories.[].categoryName").type(
                                JsonFieldType.STRING)
                            .description("???????????? ??????"),
                        fieldWithPath(
                            "dailyBriefing.memberCountByAttentionCategories.[].count").type(
                                JsonFieldType.NUMBER)
                            .description("?????? ??????????????? ?????? ??????????????? ????????? ?????? ???"),
                        fieldWithPath(
                            "dailyBriefing.memberCountByAttentionCategories.[].ranking").type(
                                JsonFieldType.NUMBER)
                            .description("?????? ??????????????? ?????? ??? ?????? ???????????? ??????")
                    )
                )
            );
      }
    }
  }
}
