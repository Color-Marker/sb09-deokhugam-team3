package com.sb09.deokhugam.domain.dashboard.batch.config;

import com.sb09.deokhugam.domain.dashboard.service.PowerUserService;
import com.sb09.deokhugam.global.custom.BatchMetricsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PowerUserBatchConfigTest {

  @Mock
  private PowerUserService powerUserService;

  @InjectMocks
  private PowerUserBatchConfig batchConfig;

  @Mock
  private BatchMetricsService batchMetricsService;

  @Test
  @DisplayName("파워 유저 Tasklet이 실행되면 Service를 호출하고 FINISHED 상태를 반환한다")
  void testCalculatePowerUserTasklet() throws Exception {
    // given
    Tasklet tasklet = batchConfig.calculatePowerUserTasklet();
    StepContribution contribution = mock(StepContribution.class);
    ChunkContext chunkContext = mock(ChunkContext.class);

    // when
    RepeatStatus status = tasklet.execute(contribution, chunkContext);

    // then
    verify(powerUserService, times(1)).calculatePowerUser(any(LocalDate.class));
    assertEquals(RepeatStatus.FINISHED, status);
  }
}