package com.hyperlink.server.member.controller;


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
import com.hyperlink.server.domain.auth.oauth.GoogleAccessToken;
import com.hyperlink.server.domain.auth.oauth.GoogleAccessTokenRepository;
import com.hyperlink.server.domain.auth.token.JwtTokenProvider;
import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import com.hyperlink.server.domain.member.dto.SignUpRequest;
import java.util.List;
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

  @Test
  void signupTest() throws Exception {
//    Category develop = categoryRepository.save(new Category("develop"));
    Category beauty = categoryRepository.save(new Category("beauty"));

    String email = "rldnd1234@naver.com";
    String accessToken = jwtTokenProvider.createAccessToken(1L);

    GoogleAccessToken savedGoogleAccessToken = googleAccessTokenRepository.save(
        new GoogleAccessToken(accessToken, email, "loalhost"));

    SignUpRequest signUpRequest = new SignUpRequest(email, "Chocho", "develop",
        "ten", 1995, List.of("develop", "beauty"), "man");

    mockMvc.perform(MockMvcRequestBuilders
            .post("/members/signup")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signUpRequest)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andDo(document("members/signup",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")),
            requestFields(
                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                fieldWithPath("career").type(JsonFieldType.STRING).description("직업 분야 "),
                fieldWithPath("careerYear").type(JsonFieldType.STRING).description("경력"),
                fieldWithPath("birthYear").type(JsonFieldType.NUMBER).description("출생년도"),
                fieldWithPath("attentionCategory").type(JsonFieldType.ARRAY).description("관심목록"),
                fieldWithPath("gender").type(JsonFieldType.STRING).description("성별")),
            responseHeaders(headerWithName(HttpHeaders.SET_COOKIE).description("RefreshToken")),
            responseFields(
                fieldWithPath("accessToken").type(JsonFieldType.STRING).description("AccessToken")))
        );
  }

  @Test
  void myPageTest() throws Exception {
    Member saveMember = memberRepository.save(
        new Member("rldnd1234@naver.com", "Chocho", Career.DEVELOP, CareerYear.MORE_THAN_TEN,
            "localhost", 1995, "man"));
    String accessToken = jwtTokenProvider.createAccessToken(saveMember.getId());

    mockMvc.perform(MockMvcRequestBuilders
            .get("/members/mypage")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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
                fieldWithPath("profileUrl").type(JsonFieldType.STRING).description("프로필 url")))
        );
  }

  @Test
  void myPageInCorrectTest() throws Exception {
    String accessToken = jwtTokenProvider.createAccessToken(1000L);

    mockMvc.perform(MockMvcRequestBuilders
            .get("/members/mypage")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andDo(print());
  }
}
