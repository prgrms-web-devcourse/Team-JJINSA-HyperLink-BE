package com.hyperlink.server.global.config;

import com.hyperlink.server.domain.creator.domain.CreatorRecommendService;
import com.hyperlink.server.domain.creator.domain.vo.RecommendCreatorPool;
import com.hyperlink.server.domain.creator.exception.CreatorEmptyException;
import com.hyperlink.server.domain.member.domain.entity.Member;
import io.lettuce.core.RedisBusyException;
import java.sql.SQLException;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class BatchJobConfig {

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final EntityManagerFactory entityManagerFactory;
  private final CreatorRecommendService creatorRecommendService;

  @Bean
  public Job recommendJob() {

    return jobBuilderFactory.get("recommendJob")
        .start(step())
        .build();
  }

  @Bean
  @JobScope
  public Step step() {
    return stepBuilderFactory.get("Step")
        .startLimit(2)
        .<Member, RecommendCreatorPool>chunk(10)
        .reader(reader())
        .processor(processor())
        .writer(writer())
        .faultTolerant()
        .retryLimit(1)
        .retry(RedisBusyException.class)
        .skipLimit(1)
        .skip(NullPointerException.class)
        .noSkip(SQLException.class)
        .noSkip(CreatorEmptyException.class)
        .build();
  }

  @Bean
  @StepScope
  public JpaPagingItemReader<Member> reader() {
    log.info("Start Reader Step!");
    return new JpaPagingItemReaderBuilder<Member>()
        .pageSize(10)
        .queryString("select m from Member m")
        .entityManagerFactory(entityManagerFactory)
        .name("JpaPagingItemReader")
        .build();
  }

  @Bean
  @StepScope
  public ItemProcessor<Member, RecommendCreatorPool> processor() {
    log.info("Start Processor Step!");
    return new ItemListProcessor(creatorRecommendService);
  }

  @Bean
  @StepScope
  public ItemWriter<RecommendCreatorPool> writer() {
    log.info("Start Writer Step!");
    return new ItemWriter<RecommendCreatorPool>() {
      @Override
      public void write(List<? extends RecommendCreatorPool> items) throws Exception {
        for (RecommendCreatorPool list : items) {
          creatorRecommendService.saveRecommendCreatorPool(list);
        }
      }
    };
  }
}
