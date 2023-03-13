package com.hyperlink.server.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import com.hyperlink.server.domain.auth.token.exception.TokenNotExistsException;
import com.hyperlink.server.domain.member.application.MemberService;
import com.hyperlink.server.global.exception.ErrorResponse;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.PatternMatchUtils;

public class AuthorizationFilter implements Filter {

  private static final String[] blackList = {"/admin/*"};

  private final AuthTokenExtractor authTokenExtractor;
  private final MemberService memberService;
  private final ObjectMapper objectMapper;

  public AuthorizationFilter(AuthTokenExtractor authTokenExtractor, MemberService memberService) {
    this.authTokenExtractor = authTokenExtractor;
    this.memberService = memberService;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String requestURI = httpRequest.getRequestURI();
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    String requestMethod = httpRequest.getMethod();

    if (!requestMethod.equals("OPTIONS") && isAdminPath(requestURI)) {
      final String authorizationHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
      final String accessToken = authTokenExtractor.extractToken(authorizationHeader);
      Long memberId = authTokenExtractor.extractMemberId(accessToken).orElseThrow(
          TokenNotExistsException::new);
      if (!memberService.isAdmin(memberId)) {
        httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
        String bodyResult = objectMapper.writeValueAsString(new ErrorResponse("권한이 없는 유저입니다."));
        httpResponse.setContentType("application/json");
        httpResponse.setCharacterEncoding("utf-8");
        httpResponse.getWriter().write(bodyResult);
        return;
      }
    }
    chain.doFilter(request, response);
  }

  private boolean isAdminPath(String requestURI) {
    return PatternMatchUtils.simpleMatch(blackList, requestURI);
  }
}
