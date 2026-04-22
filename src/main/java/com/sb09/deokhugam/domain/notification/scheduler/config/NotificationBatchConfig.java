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
      long deletedCount = notificationRepository.deleteOldNotification(duration);
      log.info("배치 작업 수행 완료. {}개의 알람이 삭제되었습니다.", deletedCount);
      return RepeatStatus.FINISHED;
    };
  }
}
