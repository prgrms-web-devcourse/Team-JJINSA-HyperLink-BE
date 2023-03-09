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
import com.hyperlink.server.domain.company.dto.CompanyRegisterRequest;
import com.hyperlink.server.domain.company.dto.CompanyResponse;
import com.hyperlink.server.domain.company.dto.MailAuthVerifyRequest;
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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
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
  void sendEmailTest() throws Exception {
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
          new CompanyResponse(Long.valueOf(i), "gmail" + i));
    }

    CompanyPageResponse companyPageResponse = new CompanyPageResponse(2, 0, companies);

    given(companyService.findCompaniesForPage(0, 2))
        .willReturn(companyPageResponse);

    authSetup();

    mockMvc.perform(
            MockMvcRequestBuilders.get("/admin/companies")
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
                    fieldWithPath("currentPage").type(JsonFieldType.NUMBER)
                        .description("현재 페이지 번호"),
                    fieldWithPath("companies.[].companyId").type(JsonFieldType.NUMBER)
                        .description("회사 식별자"),
                    fieldWithPath("companies.[].companyName").type(JsonFieldType.STRING)
                        .description("회사 이름"))));
  }

  @Test
  void emailVerificationTest() throws Exception {
    authSetup();

    String email = "rldnd2637@naver.com";

    MailAuthVerifyRequest mailAuthVerifyRequest = new MailAuthVerifyRequest(email, 1234, "s3URL");

    mockMvc.perform(MockMvcRequestBuilders
            .post("/companies/verification")
            .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(mailAuthVerifyRequest)))
        .andExpect(status().isOk())
        .andDo(print())
        .andDo(document("company/verification",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")),
            requestFields(
                fieldWithPath("companyEmail").type(JsonFieldType.STRING).description("회사 이메일"),
                fieldWithPath("authNumber").type(JsonFieldType.NUMBER).description("인증 번호"),
                fieldWithPath("logoImgUrl").type(JsonFieldType.STRING)
                    .description("회사 로고 이미지 url"))));
  }

  @Test
  void changeIsUsingRecommendTest() throws Exception {
    authSetup();

    Long companyId = 1L;

    mockMvc.perform(
            RestDocumentationRequestBuilders.put("/admin/companies/{companyId}", companyId)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
        .andExpect(status().isOk())
        .andDo(print())
        .andDo(document("company/changeIsUsingRecommend",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken"))
        ));
  }

  @Test
  void registerTest() throws Exception {
    authSetup();

    CompanyRegisterRequest companyRegisterRequest = new CompanyRegisterRequest("kakao.com",
        "LogoURL", "kakao");

    mockMvc.perform(MockMvcRequestBuilders
            .post("/admin/companies")
            .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(companyRegisterRequest)))
        .andExpect(status().isOk())
        .andDo(print())
        .andDo(document("company/verification",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")),
            requestFields(
                fieldWithPath("emailAddress").type(JsonFieldType.STRING).description("회사 이메일"),
                fieldWithPath("logoImgUrl").type(JsonFieldType.STRING).description("회사 로고 이미지 url"),
                fieldWithPath("name").type(JsonFieldType.STRING)
                    .description("회사 이름"))));
  }

}