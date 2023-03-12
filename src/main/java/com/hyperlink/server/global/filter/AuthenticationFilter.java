package com.hyperlink.server.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import com.hyperlink.server.domain.auth.token.exception.TokenExpiredException;
import com.hyperlink.server.domain.auth.token.exception.TokenInvalidFormatException;
import com.hyperlink.server.global.exception.ErrorResponse;
import io.jsonwebtoken.JwtException;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.PatternMatchUtils;

@Slf4j
public class AuthenticationFilter implements Filter {

  // TODO: /test/scheduler 삭제
  private static final String[] whitelist = {"/", "/members/logout", "/members/login",
      "/members/signup", "/profile", "/actuator/health", "/members/oauth/code/google",
      "/members/access-token", "/contents/*/view", "/daily-briefing", "/contents/all",
      "/contents", "/creators", "/creators/*", "/test/scheduler-trigger/recommend", "/save", "/docs/api.html"};

  private final AuthTokenExtractor authTokenExtractor;
  private final ObjectMapper objectMapper;

  public AuthenticationFilter(AuthTokenExtractor authTokenExtractor) {
    this.authTokenExtractor = authTokenExtractor;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String requestURI = httpRequest.getRequestURI();
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    String requestMethod = httpRequest.getMethod();
    if (!requestMethod.equals("OPTIONS") && isLoginCheckPath(requestURI)) {
      try {
        final String authorizationHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
          createAuthenticationErrorResponse(httpResponse, "인증 헤더값이 유효하지 않습니다.");
          return;
        }
        final String accessToken = authTokenExtractor.extractToken(authorizationHeader);
        authTokenExtractor.validateExpiredToken(accessToken);
      } catch (JwtException e) {
        createAuthenticationErrorResponse(httpResponse, "인증 헤더값이 유효하지 않습니다.");
        return;
      } catch (TokenInvalidFormatException | TokenExpiredException e) {
        createAuthenticationErrorResponse(httpResponse, e.getMessage());
        return;
      }
    }
    chain.doFilter(request, response);
  }

  private boolean isLoginCheckPath(String requestURI) {
    return !PatternMatchUtils.simpleMatch(whitelist, requestURI);
  }

  private void createAuthenticationErrorResponse(HttpServletResponse httpResponse, String message)
      throws IOException {
    String bodyResult = objectMapper.writeValueAsString(new ErrorResponse(message));
    httpResponse.setCharacterEncoding("utf-8");
    httpResponse.getWriter().write(bodyResult);
    httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
  }
}
