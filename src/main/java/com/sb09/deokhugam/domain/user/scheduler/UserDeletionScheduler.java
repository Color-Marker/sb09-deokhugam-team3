package com.sb09.deokhugam.domain.user.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//userdeletionscheduler는 물리삭제
//UserBatchConfig,UserDeletionScheduler는 서비스에서 쓰이는게 아니고 백그라운드, 관리용 자동화 영역에서 작동됨
//유저가 웹사이트를 이용하는 것과 상관없이, 서버가 켜져 있는 동안 뒷단(Background)에서 하루마다 조용히 실행된다
@Component
@Slf4j
//이 클래스는 정의된 Batch Job을 주기적으로 실행시키는 역할
public class UserDeletionScheduler {

  private final JobLauncher jobLauncher;
  private final Job deleteUserJob;

  public UserDeletionScheduler(JobLauncher jobLauncher,
      @Qualifier("deleteUserJob") Job
          deleteUserJob) {
    this.jobLauncher = jobLauncher;
    this.deleteUserJob = deleteUserJob;
  }

  //Batch Job은 단순히 메서드를 호출한다고 실행x
  //JobLauncher를 통해 실행해야 하며,
  //동일한 작업으로 간주되지 않도록 현재 시간을 파라미터로 넘겨 매번 새로운 실행(JobInstance)으로 기록되게 함
  @Scheduled(cron = "0 0 0 * * *")  // 매일 00시 실행
  public void processScheduledDeletion() {
    try {
      JobParameters jobParameters = new JobParametersBuilder()
          .addLong("time", System.currentTimeMillis())
          .toJobParameters();
      jobLauncher.run(deleteUserJob, jobParameters);
    } catch (Exception e) {
      log.error("유저 삭제 배치 작업 중 오류가 발생하였습니다. 세부 내용: {}", e.getMessage());
    }
  }
}
