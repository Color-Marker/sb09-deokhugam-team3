package com.sb09.deokhugam.domain.dashboard.batch.config;

import com.sb09.deokhugam.domain.dashboard.service.PopularReviewService;
import com.sb09.deokhugam.global.custom.BatchMetricsService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PopularReviewBatchConfig {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager platformTransactionManager;
  private final PopularReviewService popularReviewService;
  private final BatchMetricsService batchMetricsService;


  @Bean
  public Job calculatePopularReviewJob(Step calculatePopularReviewStep){
    return new JobBuilder("calculatePopularReviewJob", jobRepository)
        .start(calculatePopularReviewStep)
        .build();
  }

  @Bean
  public Step calculatePopularReviewStep(Tasklet calculatePopularReviewTasklet){
    return new StepBuilder("calculatePopularReviewStep", jobRepository)
        .tasklet(calculatePopularReviewTasklet, platformTransactionManager)
        .build();
  }

  @Bean
  public Tasklet calculatePopularReviewTasklet(){
    return (contribution, chunkContext) -> {
      LocalDate baseDate = LocalDate.now();
      long createdCount = popularReviewService.calculatePopularReview(baseDate);
      batchMetricsService.recordCreated("popularReview",createdCount);
      log.info("배치 작업 수행 완료. 인기 리뷰 랭킹이 저장되었습니다.");
      return RepeatStatus.FINISHED;
    };
  }
}