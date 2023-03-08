package com.hyperlink.server.member.controller;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.AuthSetupForMock;
import com.hyperlink.server.domain.attentionCategory.dto.AttentionCategoryRequest;
import com.hyperlink.server.domain.attentionCategory.dto.AttentionCategoryResponse;
import com.hyperlink.server.domain.auth.application.AuthService;
import com.hyperlink.server.domain.auth.oauth.GoogleAccessTokenRepository;
import com.hyperlink.server.domain.auth.token.RefreshTokenCookieProvider;
import com.hyperlink.server.domain.member.application.MemberService;
import com.hyperlink.server.domain.member.controller.MemberController;
import com.hyperlink.server.domain.member.dto.MembersUpdateRequest;
import com.hyperlink.server.domain.member.dto.MembersUpdateResponse;
import com.hyperlink.server.domain.member.dto.MyPageResponse;
import com.hyperlink.server.domain.member.dto.ProfileImgRequest;
import com.hyperlink.server.global.config.LoginMemberIdArgumentResolver;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
  AuthService authService;

  @MockBean
  MemberService memberService;

  @MockBean
  LoginMemberIdArgumentResolver loginMemberIdArgumentResolver;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Test
  void myInfoMockTest() throws Exception {
    authSetup();

    MyPageResponse myPageResponse = new MyPageResponse("rldnd1234@naver.com", "Chocho",
        "develop", "1",
        "localhost");

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
  void changeAttentionCategoryTest() throws Exception {
    authSetup();

    List<String> nameList = Arrays.asList("develop", "beauty");
    AttentionCategoryRequest attentionCategoryRequest = new AttentionCategoryRequest(nameList);
    AttentionCategoryResponse attentionCategoryResponse = new AttentionCategoryResponse(nameList);
    given(memberService.changeAttentionCategory(memberId, attentionCategoryRequest))
        .willReturn(attentionCategoryResponse);

    mockMvc.perform(MockMvcRequestBuilders
            .put("/members/attention-category")
            .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(attentionCategoryRequest)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andDo(document("members/attention-category",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("AccessToken")),
            responseFields(
                fieldWithPath("attentionCategory").type(JsonFieldType.ARRAY)
                    .description("관심 카테고리 목록"))));
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
