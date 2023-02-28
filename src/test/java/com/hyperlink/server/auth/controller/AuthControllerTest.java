package com.hyperlink.server.auth.controller;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.domain.auth.dto.LoginRequest;
import com.hyperlink.server.domain.auth.oauth.GoogleAccessToken;
import com.hyperlink.server.domain.auth.oauth.GoogleAccessTokenRepository;
import com.hyperlink.server.domain.auth.token.JwtTokenProvider;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@SpringBootTest
class AuthControllerTest {

  @Autowired
  private MemberRepository memberRepository;
  
  @Autowired
  private GoogleAccessTokenRepository googleAccessTokenRepository;

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;


  @DisplayName("")
  @Test
  void loginTest() throws Exception {
    String email = "rldnd1234@naver.com";
    Member saveMember = memberRepository.save(
        new Member(email, "Chocho", "develop", "10", "localhost", 1995));

    LoginRequest loginRequest = new LoginRequest(email);
    String accessToken = jwtTokenProvider.createAccessToken(saveMember.getId());

    GoogleAccessToken savedGoogleAccessToken = googleAccessTokenRepository.save(
        new GoogleAccessToken(accessToken, email));

    mockMvc.perform(MockMvcRequestBuilders
            .post("/members/login")
            .header(HttpHeaders.AUTHORIZATION,
                "Bearer " + savedGoogleAccessToken.getGoogleAccessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andDo(document("members/login",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")),
            requestFields(
                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일")),
            responseHeaders(headerWithName(HttpHeaders.SET_COOKIE).description("RefreshToken")),
            responseFields(
                fieldWithPath("accessToken").type(JsonFieldType.STRING).description("AccessToken")))
        );
  }
}