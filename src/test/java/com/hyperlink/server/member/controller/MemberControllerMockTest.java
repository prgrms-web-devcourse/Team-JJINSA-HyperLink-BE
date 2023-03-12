package com.hyperlink.server.member.controller;

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
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.AuthSetupForMock;
import com.hyperlink.server.domain.auth.application.AuthService;
import com.hyperlink.server.domain.auth.oauth.GoogleAccessToken;
import com.hyperlink.server.domain.auth.oauth.GoogleAccessTokenRepository;
import com.hyperlink.server.domain.auth.oauth.GoogleOauthClient;
import com.hyperlink.server.domain.auth.token.RefreshTokenCookieProvider;
import com.hyperlink.server.domain.member.controller.MemberController;
import com.hyperlink.server.domain.member.domain.Career;
import com.hyperlink.server.domain.member.domain.CareerYear;
import com.hyperlink.server.domain.member.dto.MembersUpdateRequest;
import com.hyperlink.server.domain.member.dto.MembersUpdateResponse;
import com.hyperlink.server.domain.member.dto.MyPageResponse;
import com.hyperlink.server.domain.member.dto.ProfileImgRequest;
import com.hyperlink.server.domain.member.dto.SignUpRequest;
import com.hyperlink.server.domain.member.dto.SignUpResult;
import com.hyperlink.server.global.config.LoginMemberIdArgumentResolver;
import java.util.List;
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
@WebMvcTest(controllers = MemberController.class)
public class MemberControllerMockTest extends AuthSetupForMock {

  @MockBean
  GoogleAccessTokenRepository googleAccessTokenRepository;

  @MockBean
  RefreshTokenCookieProvider refreshTokenCookieProvider;

  @MockBean
  GoogleOauthClient googleOauthClient;

  @MockBean
  AuthService authService;

  @MockBean
  LoginMemberIdArgumentResolver loginMemberIdArgumentResolver;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Test
  void signupTest() throws Exception {
    String email = "rldnd1234@gmail.com";
    String profileUrl = "profileUrl";
    String refreshToken = "refreshToken";

    SignUpRequest signUpRequest = new SignUpRequest(email, "Chocho",
        Career.DEVELOP.getValue(),
        CareerYear.ONE.getValue(), 1995, List.of("develop", "beauty"), "man");

    SignUpResult signUpResult = new SignUpResult(memberId, accessToken, refreshToken);
    GoogleAccessToken googleAccessToken = new GoogleAccessToken(accessToken, email, profileUrl);

    given(authService.googleTokenFindById(any())).willReturn(googleAccessToken);
    given(memberService.signUp(signUpRequest, profileUrl)).willReturn(signUpResult);

    ResponseCookie responseCookie = ResponseCookie.from(refreshToken, refreshToken)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .sameSite(SameSite.NONE.attributeValue()).maxAge(360000L).build();

    given(refreshTokenCookieProvider.createCookie(signUpResult.refreshToken())).willReturn(
        responseCookie);

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
  void myInfoMockTest() throws Exception {
    authSetup();

    MyPageResponse myPageResponse = new MyPageResponse("rldnd1234@naver.com", "Chocho",
        "develop", "1",
        "localhost", "companyEmail");

    given(memberService.myInfo(1L))
        .willReturn(myPageResponse);

    mockMvc.perform(MockMvcRequestBuilders
            .get("/members/mypage")
            .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andDo(print());
  }

  @Test
  void changeProfileImgTest() throws Exception {
    authSetup();

    ProfileImgRequest profileImgRequest = new ProfileImgRequest("profileUrl");

    mockMvc.perform(MockMvcRequestBuilders
            .put("/members/profile-image")
            .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(profileImgRequest)))
        .andExpect(status().isOk())
        .andDo(print())
        .andDo(document("members/profile-image",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")),
            requestFields(fieldWithPath("profileImgUrl").description("변경된 프로필 이미지 URL"))));
  }

  @Test
  void changeProfileTest() throws Exception {
    authSetup();

    String nickname = "master";
    String career = "develop";
    String careerYear = "one";

    MembersUpdateRequest membersUpdateRequest = new MembersUpdateRequest(nickname, career,
        careerYear);

    MembersUpdateResponse membersUpdateResponse = new MembersUpdateResponse(nickname, career,
        careerYear);

    given(memberService.changeProfile(memberId, membersUpdateRequest)).willReturn(
        membersUpdateResponse);

    mockMvc.perform(MockMvcRequestBuilders
            .put("/members/update")
            .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(membersUpdateRequest)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andDo(document("members/update",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")),
            responseFields(
                fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                fieldWithPath("career").type(JsonFieldType.STRING).description("직업 분야"),
                fieldWithPath("careerYear").type(JsonFieldType.STRING).description("경력"))));
  }
}
