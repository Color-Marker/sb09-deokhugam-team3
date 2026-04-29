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
public class PowerUserBatchScheduler {
  private final JobLauncher jobLauncher;
  private final Job calculatePowerUserJob;

  //@Scheduled(cron = "0 10 0 * * *") // 매일 00시 10분 실행
  @Scheduled(cron = "0 */10 * * * *") // 테스트용입니다.

  public void calculatePowerUser(){
    try{
      JobParameters jobParameters = new JobParametersBuilder()
          .addLong("time", System.currentTimeMillis())
          .toJobParameters();
      jobLauncher.run(calculatePowerUserJob, jobParameters);
    } catch (Exception e){
      log.error("파워 유저 배치 작업 중 오류가 발생하였습니다. 세부 내용: {}", e.getMessage());
    }
  }
}