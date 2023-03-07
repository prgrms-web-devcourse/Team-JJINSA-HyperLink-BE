package com.hyperlink.server.company.controller;


import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.AuthSetupForMock;
import com.hyperlink.server.domain.company.application.CompanyService;
import com.hyperlink.server.domain.company.controller.CompanyController;
import com.hyperlink.server.domain.company.dto.MailRequest;
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
  void myInfoMockTest() throws Exception {
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
                fieldWithPath("email").type(JsonFieldType.STRING).description("회사 이메일"))));
  }

}