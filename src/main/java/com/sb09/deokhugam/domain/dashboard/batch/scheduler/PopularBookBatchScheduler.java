package com.sb09.deokhugam.domain.dashboard.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PopularBookBatchScheduler {
  private final JobLauncher jobLauncher;
  private final Job calculatePopularBookJob;

//  @Scheduled(cron = "0 15 0 * * *")
  @Scheduled(cron = "0 */10 * * * *") // 테스트용입니다.
  public void calculatePopularBook(){
    try{
      JobParameters jobParameters = new JobParametersBuilder()
          .addLong("time", System.currentTimeMillis())
          .toJobParameters();
      jobLauncher.run(calculatePopularBookJob, jobParameters);
    } catch (Exception e){
      log.error("인기 도서 배치 작업 중 오류가 발생하였습니다. 세부 내용: {}", e.getMessage());
    }
  }
}