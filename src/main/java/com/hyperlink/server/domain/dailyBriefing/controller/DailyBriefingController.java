package com.hyperlink.server.domain.dailyBriefing.controller;

import com.hyperlink.server.domain.dailyBriefing.application.DailyBriefingService;
import com.hyperlink.server.domain.dailyBriefing.dto.GetDailyBriefingResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DailyBriefingController {

  private final DailyBriefingService dailyBriefingService;

  @GetMapping("/daily-briefing")
  @ResponseStatus(HttpStatus.OK)
  public GetDailyBriefingResponse getDailyBriefing() {
    LocalDateTime now = LocalDateTime.now();
    return dailyBriefingService.getDailyBriefingResponse(now);
  }
}
