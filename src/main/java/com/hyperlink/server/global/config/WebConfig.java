package com.hyperlink.server.global.config;

import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import com.hyperlink.server.domain.member.application.MemberService;
import com.hyperlink.server.global.filter.AuthenticationFilter;
import com.hyperlink.server.global.filter.AuthorizationFilter;
import java.util.List;
import javax.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private static final String CORS_ALLOWED_METHODS = "GET,POST,HEAD,PUT,PATCH,DELETE,TRACE,OPTIONS";
  private static final String FRONT_DOMAIN = "https://hyperlink-five.vercel.app";
  private static final String FRONTEND_LOCALHOST = "http://localhost:5173";
  private static final String FRONTEND_TEMPORARY_DOMAIN = "https://hyper-link.netlify.app";

  private final AuthTokenExtractor authTokenExtractor;
  private final MemberService memberService;

  public WebConfig(AuthTokenExtractor authTokenExtractor, MemberService memberService) {
    this.authTokenExtractor = authTokenExtractor;
    this.memberService = memberService;
  }

  @Bean
  public FilterRegistrationBean authenticationFilter() {

    FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<Filter>();

    filterRegistrationBean.setFilter(
        new AuthenticationFilter(authTokenExtractor));
    filterRegistrationBean.setOrder(1);
    filterRegistrationBean.addUrlPatterns("/*");
    return filterRegistrationBean;

  }

  @Bean
  public FilterRegistrationBean authorizationFilter() {

    FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<Filter>();

    filterRegistrationBean.setFilter(
        new AuthorizationFilter(authTokenExtractor, memberService));
    filterRegistrationBean.setOrder(2);
    filterRegistrationBean.addUrlPatterns("/*");
    return filterRegistrationBean;

  }

  @Override
  public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(new LoginMemberIdArgumentResolver(authTokenExtractor));
  }

  @Override
  public void addCorsMappings(final CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedMethods(CORS_ALLOWED_METHODS.split(","))
        .allowedOrigins(FRONT_DOMAIN, FRONTEND_LOCALHOST, FRONTEND_TEMPORARY_DOMAIN)
        .allowedHeaders("*")
        .allowCredentials(true)
        .exposedHeaders(HttpHeaders.LOCATION, HttpHeaders.SET_COOKIE);
  }
}
