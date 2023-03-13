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
import com.hyperlink.server.domain.auth.dto.RenewResult;
import com.hyperlink.server.domain.auth.oauth.GoogleAccessToken;
import com.hyperlink.server.domain.auth.oauth.GoogleOauthClient;
import com.hyperlink.server.domain.auth.token.RefreshTokenCookieProvider;
import javax.servlet.http.Cookie;
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
    String refreshToken = "REFRESH_TOKEN";
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
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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

  @DisplayName("로그아웃을 통해 refreshToken을 지울 수 있다.")
  @Test
  void logoutCorrectTest() throws Exception {

    String refreshToken = "REFRESH_TOKEN";

    RenewResult renewResult = new RenewResult(false, refreshToken, accessToken);
    Cookie cookie = new Cookie("refreshToken", refreshToken);
    ResponseCookie responseCookie = ResponseCookie.from(refreshToken, "")
        .httpOnly(true)
        .secure(true)
        .path("/")
        .sameSite(SameSite.NONE.attributeValue()).maxAge(360000L).build();

    given(refreshTokenCookieProvider.createLogoutCookie())
        .willReturn(responseCookie);

    mockMvc.perform(MockMvcRequestBuilders
            .post("/members/logout")
            .cookie(cookie))
        .andExpect(status().isOk())
        .andDo(print())
        .andDo(document("members/logout",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(headerWithName("Cookie").optional().description("RefreshToken")),
            responseHeaders(
                headerWithName(HttpHeaders.SET_COOKIE).description("logout Refresh Token"))));
  }

  @DisplayName("refreshToken을 통해 accessToken을 재발급 받을 수 있다.")
  @Test
  void renewTest() throws Exception {
    String refreshToken = "REFRESH_TOKEN";
    Cookie cookie = new Cookie("refreshToken", refreshToken);

    RenewResult renewResult = new RenewResult(false, accessToken, refreshToken);
    given(authService.renewTokens(refreshToken)).willReturn(renewResult);

    ResponseCookie responseCookie = ResponseCookie.from(refreshToken, refreshToken)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .sameSite(SameSite.NONE.attributeValue()).maxAge(360000L).build();

    given(refreshTokenCookieProvider.createCookie(refreshToken)).willReturn(
        responseCookie);

    mockMvc.perform(MockMvcRequestBuilders
            .get("/members/access-token")
            .cookie(cookie))
        .andExpect(status().isOk())
        .andDo(print())
        .andDo(document("members/access-token",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(headerWithName("Cookie").optional().description("RefreshToken")),
            responseHeaders(
                headerWithName(HttpHeaders.SET_COOKIE).description("logout Refresh Token")),
            responseFields(
                fieldWithPath("admin").type(JsonFieldType.BOOLEAN).description("관리자 여부"),
                fieldWithPath("accessToken").type(JsonFieldType.STRING).description("AccessToken")))
        );
  }
}
