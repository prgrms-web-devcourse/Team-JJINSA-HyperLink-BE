package com.hyperlink.server.company.controller;


import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.AuthSetupForMock;
import com.hyperlink.server.domain.company.application.CompanyService;
import com.hyperlink.server.domain.company.controller.CompanyController;
import com.hyperlink.server.domain.company.dto.CompanyPageResponse;
import com.hyperlink.server.domain.company.dto.CompanyResponse;
import com.hyperlink.server.domain.company.dto.MailRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureRestDocs
@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(controllers = CompanyController.class)
class CompanyControllerTest extends AuthSetupForMock {

  @MockBean
  CompanyService companyService;

  @MockBean
  JavaMailSender javaMailSender;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;


  @Test
  void sendMailTest() throws Exception {
    authSetup();

    String email = "rldnd2637@naver.com";

    MailRequest mailRequest = new MailRequest(email);

    mockMvc.perform(MockMvcRequestBuilders
            .post("/companies/auth")
            .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(mailRequest)))
        .andExpect(status().isOk())
        .andDo(print())
        .andDo(document("company/auth",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")),
            requestFields(
                fieldWithPath("companyEmail").type(JsonFieldType.STRING).description("회사 이메일"))));
  }

  @Test
  void getCompanyPageTest() throws Exception {
    List<CompanyResponse> companies = new ArrayList<>();
    for (int i = 1; i <= 3; i++) {
      companies.add(
          new CompanyResponse(Long.valueOf(i), "gmail.com" + i, "gmail" + i));
    }

    CompanyPageResponse companyPageResponse = new CompanyPageResponse(2, companies);

    given(companyService.findCompaniesForPage(0, 2))
        .willReturn(companyPageResponse);

    authSetup();

    mockMvc.perform(
            MockMvcRequestBuilders.get("/companies")
                .param("page", "0")
                .param("size", "2")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
        )
        .andExpect(status().isOk())
        .andDo(print())
        .andDo(
            document(
                "company/getCompanyPage",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                    headerWithName(HttpHeaders.AUTHORIZATION).description("jwt header")),
                responseFields(
                    fieldWithPath("totalPage").type(JsonFieldType.NUMBER)
                        .description("전체 페이지 수"),
                    fieldWithPath("companies.[].companyId").type(JsonFieldType.NUMBER)
                        .description("회사 식별자"),
                    fieldWithPath("companies.[].emailAddress").type(JsonFieldType.STRING)
                        .description("회사 이메일주소"),
                    fieldWithPath("companies.[].companyName").type(JsonFieldType.STRING)
                        .description("회사 이름"))));


  }

}