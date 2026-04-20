package com.sb09.deokhugam.domain.dashboard.schedular.config;

import com.sb09.deokhugam.domain.dashboard.service.PopularBookService;
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
public class PopularBookBatchConfig {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager platformTransactionManager;
  private final PopularBookService popularBookService;

  @Bean
  public Job calculatePopularBookJob(Step calculatePopularBookStep){
    return new JobBuilder("calculatePopularBookJob", jobRepository)
        .start(calculatePopularBookStep)
        .build();
  }

  @Bean
  public Step calculatePopularBookStep(Tasklet calculatePopularBookTasklet){
    return new StepBuilder("calculatePopularBookStep", jobRepository)
        .tasklet(calculatePopularBookTasklet, platformTransactionManager)
        .build();
  }

  @Bean
  public Tasklet calculatePopularBookTasklet(){
    return (contribution, chunkContext) -> {
      LocalDate baseDate = LocalDate.now();
      popularBookService.calculatePopularBook(baseDate);
      log.info("배치 작업 수행 완료. 인기 도서 랭킹이 저장되었습니다.");
      return RepeatStatus.FINISHED;
    };
  }
}
