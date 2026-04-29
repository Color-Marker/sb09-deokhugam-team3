package com.sb09.deokhugam.domain.dashboard.batch.config;

import com.sb09.deokhugam.domain.dashboard.service.PowerUserService;
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
public class PowerUserBatchConfig {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager platformTransactionManager;
  private final PowerUserService powerUserService;
  private final BatchMetricsService batchMetricsService;

  @Bean
  public Job calculatePowerUserJob(Step calculatePowerUserStep){
    return new JobBuilder("calculatePowerUserJob", jobRepository)
        .start(calculatePowerUserStep)
        .build();
  }

  @Bean
  public Step calculatePowerUserStep(Tasklet calculatePowerUserTasklet){
    return new StepBuilder("calculatePowerUserStep", jobRepository)
        .tasklet(calculatePowerUserTasklet, platformTransactionManager)
        .build();
  }

  @Bean
  public Tasklet calculatePowerUserTasklet(){
    return (contribution, chunkContext) -> {
      LocalDate baseDate = LocalDate.now();
      powerUserService.calculatePowerUser(baseDate);
      batchMetricsService.recordCreated("powerUser");
      log.info("배치 작업 수행 완료. 파워 유저 랭킹이 저장되었습니다.");
      return RepeatStatus.FINISHED;
    };
  }
}