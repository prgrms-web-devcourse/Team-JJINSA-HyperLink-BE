package com.hyperlink.server.global.exception;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.domain.auth.oauth.GoogleAccessToken;
import com.hyperlink.server.domain.auth.oauth.GoogleAccessTokenRepository;
import com.hyperlink.server.domain.auth.oauth.dto.OauthResponse;
import com.hyperlink.server.domain.auth.token.JwtTokenProvider;
import com.hyperlink.server.domain.category.domain.CategoryRepository;
import com.hyperlink.server.domain.category.domain.entity.Category;
import com.hyperlink.server.domain.member.dto.SignUpRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GlobalExceptionHandlerTest {

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private GoogleAccessTokenRepository googleAccessTokenRepository;

  @Autowired
  private JwtTokenProvider jwtTokenProvider;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Test
  void MethodArgumentNotValidExceptionTest() throws Exception {
    String accessToken = jwtTokenProvider.createAccessToken(1L);
    SignUpRequest signUpRequest = new SignUpRequest(" ",
        "chocho",
        "develop",
        "10", 1995,
        List.of("develop", "beauty"), "man");

    mockMvc.perform(MockMvcRequestBuilders
            .post("/members/signup")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signUpRequest)))
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @Test
  void handleInvalidRequestBodyTest() throws Exception {
    String accessToken = jwtTokenProvider.createAccessToken(1L);

    mockMvc.perform(MockMvcRequestBuilders
            .post("/members/signup")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @Test
  void handleTypeMismatchTest() throws Exception {
    String accessToken = jwtTokenProvider.createAccessToken(1L);

    mockMvc.perform(MockMvcRequestBuilders
            .post("/bookmark/" + 1L + "?type=1234")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @Test
  void handleNotSupportedMethodTest() throws Exception {

    Category develop = categoryRepository.save(new Category("develop"));
    Category beauty = categoryRepository.save(new Category("beauty"));

    String email = "rldnd1234@naver.com";
    String profileUrl = "profileUrl";
    String accessToken = jwtTokenProvider.createAccessToken(1L);

    GoogleAccessToken savedGoogleAccessToken = googleAccessTokenRepository.save(
        new GoogleAccessToken(accessToken, email, profileUrl));
    OauthResponse oauthResponse = new OauthResponse(accessToken, true, email);

    SignUpRequest signUpRequest = new SignUpRequest(email, "Chocho", "develop",
        "10", 1995, List.of("develop", "beauty"), "man");

    mockMvc.perform(MockMvcRequestBuilders
            .get("/members/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signUpRequest)))
        .andExpect(status().isMethodNotAllowed())
        .andDo(print());
  }

}