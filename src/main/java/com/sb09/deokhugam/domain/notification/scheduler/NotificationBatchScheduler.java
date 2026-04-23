package com.sb09.deokhugam.domain.notification.scheduler;

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
public class NotificationBatchScheduler {
  private final JobLauncher jobLauncher;
  private final Job deleteNotificationJob;

  @Scheduled(cron = "0 0 2 * * *")
  public void deleteOldNotification(){
    try{
      JobParameters jobParameters = new JobParametersBuilder()
          .addLong("time", System.currentTimeMillis())
          .toJobParameters();
      jobLauncher.run(deleteNotificationJob, jobParameters);
    } catch (Exception e){
      log.error("알림 삭제 배치 작업 중 오류가 발생하였습니다. 세부 내용: {}", e.getMessage());
    }
  }
}
