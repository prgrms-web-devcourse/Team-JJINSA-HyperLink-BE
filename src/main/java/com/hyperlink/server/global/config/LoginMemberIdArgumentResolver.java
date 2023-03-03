package com.hyperlink.server.global.config;

import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import java.util.Optional;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginMemberIdArgumentResolver implements HandlerMethodArgumentResolver {

  private static final int TOKEN_VALUE_INDEX = 1;
  private final AuthTokenExtractor authTokenExtractor;

  public LoginMemberIdArgumentResolver(AuthTokenExtractor authTokenExtractor) {
    this.authTokenExtractor = authTokenExtractor;
  }

  @Override
  public boolean supportsParameter(final MethodParameter parameter) {
    return parameter.hasParameterAnnotation(LoginMemberId.class)
        && parameter.getParameterType().equals(Optional.class);
  }

  @Override
  public Optional<Long> resolveArgument(final MethodParameter parameter,
      final ModelAndViewContainer mavContainer,
      final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) {
    String authorizationHeader = webRequest.getHeader("Authorization");
    if (authorizationHeader == null || authorizationHeader.isBlank()) {
      return Optional.empty();
    }
    String accessToken = authorizationHeader.split("Bearer ")[TOKEN_VALUE_INDEX];
    return authTokenExtractor.extractMemberId(accessToken);
  }
}