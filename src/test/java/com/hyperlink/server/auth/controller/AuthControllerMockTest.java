package com.hyperlink.server.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
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
import com.hyperlink.server.AuthSetupForMock;
import com.hyperlink.server.domain.auth.application.AuthService;
import com.hyperlink.server.domain.auth.controller.AuthController;
import com.hyperlink.server.domain.auth.dto.LoginResult;
import com.hyperlink.server.domain.auth.oauth.GoogleAccessToken;
import com.hyperlink.server.domain.auth.oauth.GoogleOauthClient;
import com.hyperlink.server.domain.auth.token.RefreshTokenCookieProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureRestDocs
@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(controllers = AuthController.class)
public class AuthControllerMockTest extends AuthSetupForMock {

  @MockBean
  private RefreshTokenCookieProvider refreshTokenCookieProvider;
  @MockBean
  private AuthService authService;

  @MockBean
  protected GoogleOauthClient googleOauthClient;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @DisplayName("로그인을 통해 인증토큰을 받을 수 있다.")
  @Test
  void loginCorrectTest() throws Exception {
    String email = "rldnd1234@gmail.com";
    String profileUrl = "profileUrl";
    String refreshToken = "refreshToken";
    Boolean admin = false;

    GoogleAccessToken googleAccessToken = new GoogleAccessToken(accessToken, email, profileUrl);
    given(authService.googleTokenFindById(any())).willReturn(googleAccessToken);

    LoginResult loginResult = new LoginResult(admin, accessToken, refreshToken);
    given(authService.login(googleAccessToken)).willReturn(loginResult);

    ResponseCookie responseCookie = ResponseCookie.from(refreshToken, refreshToken)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .sameSite(SameSite.NONE.attributeValue()).maxAge(360000L).build();

    given(refreshTokenCookieProvider.createCookie(loginResult.refreshToken())).willReturn(
        responseCookie);

    mockMvc.perform(MockMvcRequestBuilders
            .post("/members/login")
            .header(HttpHeaders.AUTHORIZATION,
                "Bearer " + googleAccessToken.getGoogleAccessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=utf-8"))
        .andDo(print())
        .andDo(document("members/login",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")),
            responseHeaders(headerWithName(HttpHeaders.SET_COOKIE).description("RefreshToken")),
            responseFields(
                fieldWithPath("admin").type(JsonFieldType.BOOLEAN).description("관리자 여부"),
                fieldWithPath("accessToken").type(JsonFieldType.STRING).description("AccessToken")))
        );
  }


}
