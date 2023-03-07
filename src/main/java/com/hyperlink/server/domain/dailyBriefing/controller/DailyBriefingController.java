package com.hyperlink.server.domain.dailyBriefing.controller;

import com.hyperlink.server.domain.dailyBriefing.dto.GetDailyBriefingResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DailyBriefingController {

  private final RedisTemplate<String, GetDailyBriefingResponse> dailyBriefingResponseRedisTemplate;

  @GetMapping("/daily-briefing")
  @ResponseStatus(HttpStatus.OK)
  public GetDailyBriefingResponse getDailyBriefing() {
    LocalDateTime now = LocalDateTime.now();
    String standardTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH"));
    return (GetDailyBriefingResponse) dailyBriefingResponseRedisTemplate.opsForHash()
        .get("daily-briefing", standardTime);
  }
}
