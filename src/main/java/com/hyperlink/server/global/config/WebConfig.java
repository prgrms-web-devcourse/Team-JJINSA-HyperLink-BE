package com.hyperlink.server.global.config;

import com.hyperlink.server.domain.auth.token.AuthTokenExtractor;
import com.hyperlink.server.domain.auth.token.JwtTokenProvider;
import com.hyperlink.server.global.filter.AuthenticationFilter;
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

  private final AuthTokenExtractor authTokenExtractor;
  private final JwtTokenProvider jwtTokenProvider;

  public WebConfig(AuthTokenExtractor authTokenExtractor, JwtTokenProvider jwtTokenProvider) {
    this.authTokenExtractor = authTokenExtractor;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Bean
  public FilterRegistrationBean loginFilter() {
    FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<Filter>();

    filterRegistrationBean.setFilter(
        new AuthenticationFilter(authTokenExtractor, jwtTokenProvider));
    filterRegistrationBean.setOrder(1);
    filterRegistrationBean.addUrlPatterns("/*");
    return filterRegistrationBean;
  }

  @Override
  public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(new LoginMemberIdArgumentResolver(authTokenExtractor));
  }

  @Override
  public void addCorsMappings(final CorsRegistry registry) {
    registry.addMapping("/**") //추후 변동예정.
        .allowedMethods(CORS_ALLOWED_METHODS.split(","))
        .allowedOrigins("*")
        .allowedHeaders("*")
        .allowCredentials(false)
        .exposedHeaders(HttpHeaders.LOCATION, HttpHeaders.SET_COOKIE);
  }

}
