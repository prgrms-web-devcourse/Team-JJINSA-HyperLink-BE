package com.hyperlink.server.auth.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.domain.auth.oauth.GoogleAccessTokenRepository;
import com.hyperlink.server.domain.auth.token.JwtTokenProvider;
import com.hyperlink.server.domain.auth.token.RefreshToken;
import com.hyperlink.server.domain.auth.token.RefreshTokenRepository;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.domain.MemberRepository;
import com.hyperlink.server.domain.member.domain.entity.Member;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@Transactional
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@SpringBootTest
class AuthControllerTest {

  @Autowired
  private GoogleAccessTokenRepository googleAccessTokenRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @DisplayName("로그인시 accessToken이 존재하지않는다면 401을 반환한다.")
  @Test
  void loginInCorrectTest() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
            .post("/members/login")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType("application/json;charset=utf-8"))
        .andDo(print());
  }

  @DisplayName("로그아웃을 통해 refreshToken을 지울 수 있다.")
  @Test
  void logoutCorrectTest() throws Exception {
    String email = "rldnd1234@naver.com";
    Member saveMember = memberRepository.save(
        new Member(email, "Chocho", Career.DEVELOP, CareerYear.MORE_THAN_TEN, "localhost", 1995,
            "man"));

    RefreshToken savedRefreshToken = refreshTokenRepository.save(
        new RefreshToken(UUID.randomUUID().toString(), saveMember.getId()));

    Cookie cookie = new Cookie("refreshToken", savedRefreshToken.getRefreshToken());

    mockMvc.perform(MockMvcRequestBuilders
            .post("/members/logout")
            .cookie(cookie))
        .andExpect(status().isOk())
        .andDo(print());
  }

  @DisplayName("refhToken이 없이 로그아웃을 요청한다면 401 에러가 발생한다.")
  @Test
  void logoutInCorrectTest() throws Exception {
    String email = "rldnd1234@naver.com";
    Member saveMember = memberRepository.save(
        new Member(email, "Chocho", Career.FINANCE, CareerYear.NINE, "localhost", 1995, "man"));

    mockMvc.perform(MockMvcRequestBuilders
            .post("/members/logout"))
        .andExpect(status().isUnauthorized())
        .andDo(print());
  }

  @DisplayName("refreshToken을 통해 accessToken을 재발급 받을 수 있다.")
  @Test
  void renewTest() throws Exception {
    String email = "rldnd1234@naver.com";
    Member saveMember = memberRepository.save(
        new Member(email, "Chocho", Career.FINANCE, CareerYear.EIGHT, "localhost", 1995, "man"));

    RefreshToken savedRefreshToken = refreshTokenRepository.save(
        new RefreshToken(UUID.randomUUID().toString(), saveMember.getId()));

    Cookie cookie = new Cookie("refreshToken", savedRefreshToken.getRefreshToken());

    mockMvc.perform(MockMvcRequestBuilders
            .get("/members/access-token")
            .header(HttpHeaders.COOKIE, "refreshToken=" + savedRefreshToken.getRefreshToken())
            .cookie(cookie))
        .andExpect(status().isOk())
        .andDo(print());
  }
}