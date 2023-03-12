package com.hyperlink.server.member.controller;


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
import com.hyperlink.server.domain.auth.oauth.GoogleAccessTokenRepository;
import com.hyperlink.server.domain.auth.token.JwtTokenProvider;
import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.company.domain.CompanyRepository;
import com.hyperlink.server.domain.company.domain.entity.Company;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@SpringBootTest
public class MemberControllerTest {

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private GoogleAccessTokenRepository googleAccessTokenRepository;

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  private CompanyRepository companyRepository;


  @DisplayName("company 미인증인 경우 MyPageApi")
  @Test
  void myPageTestV1() throws Exception {
    Member saveMember = memberRepository.save(
        new Member("rldnd1234@naver.com", "Chocho", Career.DEVELOP, CareerYear.MORE_THAN_TEN,
            "localhost", 1995, "man"));
    String accessToken = jwtTokenProvider.createAccessToken(saveMember.getId());

    mockMvc.perform(MockMvcRequestBuilders
            .get("/members/mypage")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=utf-8"))
        .andDo(print())
        .andDo(document("members/mypage",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")),
            responseFields(
                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                fieldWithPath("career").type(JsonFieldType.STRING).description("직업 분야 "),
                fieldWithPath("careerYear").type(JsonFieldType.STRING).description("경력"),
                fieldWithPath("profileUrl").type(JsonFieldType.STRING).description("프로필 url"),
                fieldWithPath("companyName").type(JsonFieldType.NULL)
                    .description("회사 미인증시 null")))
        );
  }

  @DisplayName("company인증한 경우 MyPageApi")
  @Test
  void myPageTestV2() throws Exception {
    Member saveMember = memberRepository.save(
        new Member("rldnd1234@naver.com", "Chocho", Career.DEVELOP, CareerYear.MORE_THAN_TEN,
            "localhost", 1995, "man"));

    Company savedCompany = companyRepository.save(
        new Company("rldnd1234@kakao.com", "logoImgUrl", "kakao"));
    String accessToken = jwtTokenProvider.createAccessToken(saveMember.getId());

    saveMember.changeCompany(savedCompany);

    mockMvc.perform(MockMvcRequestBuilders
            .get("/members/mypage")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=utf-8"))
        .andDo(print())
        .andDo(document("members/mypage",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")),
            responseFields(
                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                fieldWithPath("career").type(JsonFieldType.STRING).description("직업 분야 "),
                fieldWithPath("careerYear").type(JsonFieldType.STRING).description("경력"),
                fieldWithPath("profileUrl").type(JsonFieldType.STRING).description("프로필 url"),
                fieldWithPath("companyName").type(JsonFieldType.STRING).description("인증된 회사 이름")))
        );
  }

  @Test
  void myPageInCorrectTest() throws Exception {
    String accessToken = jwtTokenProvider.createAccessToken(1000L);

    mockMvc.perform(MockMvcRequestBuilders
            .get("/members/mypage")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType("application/json;charset=utf-8"))
        .andDo(print());
  }
}
