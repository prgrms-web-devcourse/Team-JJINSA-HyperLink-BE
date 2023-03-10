package com.hyperlink.server.domain.dailyBriefing;

import com.hyperlink.server.domain.dailyBriefing.application.DailyBriefingService;
import com.hyperlink.server.domain.dailyBriefing.dto.GetDailyBriefingResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DailyBriefingScheduler {

  private final DailyBriefingService dailyBriefingService;
  private final RedisTemplate<String, Object> redisTemplate;

  @Scheduled(cron = "0 0 0-23 * * *", zone = "Asia/Seoul")
  void createDailyBriefing() {
    log.info("데일리브리핑 스케쥴러 실행");
    GetDailyBriefingResponse dailyBriefing = dailyBriefingService.createDailyBriefing(
        LocalDateTime.now());

    redisTemplate.opsForHash().put("daily-briefing", dailyBriefing.standardTime(), dailyBriefing);
    log.info("데일리브리핑 스케쥴러 완료");
  }

}
