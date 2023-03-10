package com.hyperlink.server.domain.auth.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.domain.auth.oauth.dto.GoogleProfileResult;
import com.hyperlink.server.domain.auth.oauth.dto.GoogleToken;
import com.hyperlink.server.domain.auth.oauth.dto.OauthResponse;
import com.hyperlink.server.domain.auth.oauth.exception.JsonProcessingCustomException;
import com.hyperlink.server.domain.member.application.MemberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class GoogleOauthClient {

  private static final String TOKEN_TYPE = "Bearer ";

  private final String clientId;
  private final String clientSecret;
  private final String redirectUri;
  private final String tokenUri;
  private final String profileUri;
  private final String revokeUri;

  private final ObjectMapper objectMapper;
  private final MemberService memberService;
  private final GoogleAccessTokenRepository googleAccessTokenRepository;

  public GoogleOauthClient(
      @Value("${google.client.id}") String clientId,
      @Value("${google.client.secret}") String clientSecret,
      @Value("${google.client.redirect}") String redirectUri,
      @Value("${google.tokenUri}") String tokenUri,
      @Value("${google.profileUri}") String profileUri,
      @Value("${google.revokeUri}") String revokeUri,
      ObjectMapper objectMapper,
      MemberService memberService, GoogleAccessTokenRepository googleAccessTokenRepository) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.redirectUri = redirectUri;
    this.tokenUri = tokenUri;
    this.profileUri = profileUri;
    this.revokeUri = revokeUri;

    this.objectMapper = objectMapper;
    this.memberService = memberService;
    this.googleAccessTokenRepository = googleAccessTokenRepository;
  }

  @GetMapping("/members/oauth/code/google")
  public ResponseEntity<OauthResponse> authGoogle(@RequestParam String code) {
    try {
      String accessToken = extractAccessToken(
          requestAccessToken(
              generateAuthCodeRequest(code)).getBody());

      String json = requestProfile(generateProfileRequest(accessToken)).getBody();
      GoogleProfileResult googleProfileResult = objectMapper.readValue(json,
          GoogleProfileResult.class);

      googleAccessTokenRepository.save(
          new GoogleAccessToken(accessToken, googleProfileResult.email(),
              googleProfileResult.picture()));

      boolean wasSignedUp = memberService.existsMemberByEmail(googleProfileResult.email());
      return ResponseEntity.ok(
          new OauthResponse(accessToken, wasSignedUp, googleProfileResult.email()));
    } catch (JsonProcessingException exception) {
      throw new JsonProcessingCustomException();
    }
  }

  private HttpEntity<MultiValueMap<String, String>> generateAuthCodeRequest(String code) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);
    params.add("code", code);
    params.add("redirect_uri", redirectUri);

    return new HttpEntity<>(params, headers);
  }

  private ResponseEntity<String> requestAccessToken(HttpEntity request) {
    RestTemplate restTemplate = new RestTemplate();
    return restTemplate.exchange(
        tokenUri,
        HttpMethod.POST,
        request,
        String.class
    );
  }

  private String extractAccessToken(String json) throws JsonProcessingException {
    GoogleToken googleToken = objectMapper.readValue(json, GoogleToken.class);
    return googleToken.access_token();
  }

  private HttpEntity<MultiValueMap<String, String>> generateProfileRequest(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, TOKEN_TYPE + accessToken);
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    return new HttpEntity<>(headers);
  }

  private ResponseEntity<String> requestProfile(HttpEntity request) {
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> req = restTemplate.exchange(
        profileUri,
        HttpMethod.GET,
        request,
        String.class
    );
    return req;
  }

  public ResponseEntity<Void> revokeToken(String accessToken) {
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders header = new HttpHeaders();
    header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
    param.add("token", accessToken);
    return restTemplate.exchange(revokeUri, HttpMethod.POST, new HttpEntity<>(param, header),
        Void.class);
  }
}
