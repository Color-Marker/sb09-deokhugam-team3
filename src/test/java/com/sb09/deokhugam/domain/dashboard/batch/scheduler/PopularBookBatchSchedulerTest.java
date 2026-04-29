package com.sb09.deokhugam.domain.dashboard.batch.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PopularBookBatchSchedulerTest {

  @Mock
  private JobLauncher jobLauncher;

  @Mock
  private Job calculatePopularBookJob;

  @InjectMocks
  private PopularBookBatchScheduler scheduler;

  @Test
  @DisplayName("인기 도서 스케줄러가 작동하면 JobLauncher가 정상적으로 Job을 실행한다")
  void calculatePopularBook_Success() throws Exception {
    // when
    scheduler.calculatePopularBook();

    // then
    verify(jobLauncher, times(1)).run(eq(calculatePopularBookJob), any(JobParameters.class));
  }
}