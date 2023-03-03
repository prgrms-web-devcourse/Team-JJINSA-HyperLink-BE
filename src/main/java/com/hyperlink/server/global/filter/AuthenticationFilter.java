package com.hyperlink.server.global.filter;

import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import com.hyperlink.server.domain.auth.token.JwtTokenProvider;
import com.hyperlink.server.domain.auth.token.exception.TokenExpiredException;
import com.hyperlink.server.domain.auth.token.exception.TokenNotExistsException;
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

  private static final String[] whitelist = {"/", "/members/logout", "/members/login",
      "/members/signup", "/profile", "/actuator/health", "/members/oauth/code/google",
      "/members/access-token"};

  private final AuthTokenExtractor authTokenExtractor;
  private final JwtTokenProvider jwtTokenProvider;

  public AuthenticationFilter(AuthTokenExtractor authTokenExtractor,
      JwtTokenProvider jwtTokenProvider) {
    this.authTokenExtractor = authTokenExtractor;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String requestURI = httpRequest.getRequestURI();
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    if (isLoginCheckPath(requestURI)) {
      hasAuthorization((httpRequest));
      try {
        final String authorizationHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        final String accessToken = authTokenExtractor.extractToken(authorizationHeader);
        authTokenExtractor.validateExpiredToken(accessToken);
      } catch (TokenExpiredException e) {
        httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        return;
      }
    }
    chain.doFilter(request, response);
  }

  private void hasAuthorization(final HttpServletRequest request) {
    if (request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
      throw new TokenNotExistsException();
    }
  }

  private boolean isLoginCheckPath(String requestURI) {
    return !PatternMatchUtils.simpleMatch(whitelist, requestURI);
  }
}
