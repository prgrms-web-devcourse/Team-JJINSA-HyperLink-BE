package com.hyperlink.server.domain.creator.controller;

import com.hyperlink.server.global.config.BatchJobConfig;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RecommendScheduler {

  private final JobLauncher jobLauncher;
  private final BatchJobConfig batchJobConfig;

  @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
  void runBatchRecommendJob() {
    log.info("RecommendJob 실행");

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

    log.info("RecommendJob 완료");
  }

}
