package com.hyperlink.server.global.profile.controller;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProfileController {

  private final Environment env;

  @GetMapping("/profile")
  public String getProfile() {
    return Arrays.stream(env.getActiveProfiles())
        .findFirst()
        .orElse("");
  }

}
