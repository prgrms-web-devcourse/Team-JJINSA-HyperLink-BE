package com.hyperlink.server.domain.admin.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperlink.server.domain.admin.application.AdminService;
import com.hyperlink.server.domain.admin.dto.CategoryViewResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminScheduler {

  private final ObjectMapper objectMapper;
  private final AdminService adminService;
  private final RedisTemplate<String, String> categoryViewRedisTemplate;
  private static final String CATEGORY_VIEW = "category-view";

  @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
  void createCategoryViewDashboardData() {
    log.info("[Admin] 카테고리 별 조회수 수집");
    CategoryViewResponses categoryViewResponses = adminService.countViewCountByCategory();

    try {
      String categoryViewResponsesJson = objectMapper.writeValueAsString(categoryViewResponses);
      categoryViewRedisTemplate.opsForValue().set(CATEGORY_VIEW, categoryViewResponsesJson);
    } catch (JsonProcessingException e) {
      log.error("[Admin] 카테고리 별 조회수 수집 실패", e);
    }
    log.info("[Admin] 카테고리 별 조회수 수집 완료");
  }

}
