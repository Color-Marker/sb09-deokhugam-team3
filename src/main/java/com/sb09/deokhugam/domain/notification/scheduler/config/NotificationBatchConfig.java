package com.sb09.deokhugam.domain.notification.scheduler.config;

import com.sb09.deokhugam.domain.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
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
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class NotificationBatchConfig {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager platformTransactionManager;
  private final NotificationRepository notificationRepository;

  @Bean
  public Job deleteNotificationJob(Step deleteNotificationStep){
    return new JobBuilder("deleteNotificationJob", jobRepository)
        .start(deleteNotificationStep)
        .build();
  }

  @Bean
  public Step deleteNotificationStep(Tasklet deleteNotificationTasklet){
    return new StepBuilder("deleteNotificationStep", jobRepository)
        .tasklet(deleteNotificationTasklet, platformTransactionManager)
        .build();
  }

  @Bean
  public Tasklet deleteNotificationTasklet(){
    return (contribution, chunkContext) -> {
      log.info("일주일이 경과된 확인된 알람에 대해 삭제 작업을 수행합니다.");
      LocalDateTime duration = LocalDateTime.now().minusWeeks(1);

      int attempt = 0;
      int maxRetry = 3;
      while (attempt < maxRetry){
        try{

          long deletedCount = notificationRepository.deleteOldNotification(duration);
          log.info("배치 작업 수행 완료. {}개의 알람이 삭제되었습니다.", deletedCount);
          return RepeatStatus.FINISHED;

        } catch(DataAccessException e){
          attempt++;
          log.warn("DB 오류 발생, 재시도 {}/{}", attempt, maxRetry, e);

          if(attempt >= maxRetry){
            log.error("최대 재시도 횟수 초과, 배치 실패");
            throw e;
          }
          log.info("{}초 후 재시도합니다.", attempt);
          Thread.sleep(5000L * attempt); // 잠시 대기 후 재시도
        }
      }
      return RepeatStatus.FINISHED;
    };
  }
}
