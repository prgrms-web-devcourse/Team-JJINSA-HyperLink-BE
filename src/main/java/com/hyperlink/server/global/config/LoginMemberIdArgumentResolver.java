package com.hyperlink.server.global.config;

import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginMemberIdArgumentResolver implements HandlerMethodArgumentResolver {

  private final AuthTokenExtractor authTokenExtractor;

  public LoginMemberIdArgumentResolver(AuthTokenExtractor authTokenExtractor) {
    this.authTokenExtractor = authTokenExtractor;
  }

  @Override
  public boolean supportsParameter(final MethodParameter parameter) {
    return parameter.hasParameterAnnotation(LoginMemberId.class)
        && parameter.getParameterType().equals(Long.class);
  }

  @Override
  public Long resolveArgument(final MethodParameter parameter,
      final ModelAndViewContainer mavContainer,
      final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) {
    String accessToken = webRequest.getHeader("Authorization").split("Bearer ")[1];
    return authTokenExtractor.extractMemberId(accessToken);
  }
}