package com.hyperlink.server.domain.admin.controller;

import com.hyperlink.server.domain.admin.application.AdminService;
import com.hyperlink.server.domain.admin.dto.CategoryViewResponses;
import com.hyperlink.server.global.config.BatchJobConfig;
import com.hyperlink.server.global.config.LoginMemberId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AdminController {

  private final AdminService adminService;
  private final JobLauncher jobLauncher;
  private final BatchJobConfig batchJobConfig;

  @GetMapping("/admin/dashboard/all-category/view")
  @ResponseStatus(HttpStatus.OK)
  public CategoryViewResponses getDailyBriefingForAdmin(@LoginMemberId Optional<Long> optionalMemberId) {
    return adminService.getCategoryView().orElseGet(adminService::countViewCountByCategory);
  }

  @GetMapping("/admin/scheduler-trigger/recommend")
  @ResponseStatus(HttpStatus.OK)
  public void startScheduler(@LoginMemberId Optional<Long> optionalMemberId) {
    log.info("RecommendJob 수동 실행");

    Map<String, JobParameter> confMap = new HashMap<>();
    confMap.put("time", new JobParameter(System.currentTimeMillis()));
    JobParameters jobParameters = new JobParameters(confMap);

    try {
      jobLauncher.run(batchJobConfig.recommendJob(), jobParameters);
    } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException
             | JobParametersInvalidException |
             org.springframework.batch.core.repository.JobRestartException e) {

      log.error(e.getMessage());
    }

    log.info("RecommendJob 수동 실행 완료");
  }
}
