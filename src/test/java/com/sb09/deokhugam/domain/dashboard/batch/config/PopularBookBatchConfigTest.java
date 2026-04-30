package com.sb09.deokhugam.domain.dashboard.batch.config;

import com.sb09.deokhugam.domain.dashboard.service.PopularBookService;
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
class PopularBookBatchConfigTest {

  @Mock
  private PopularBookService popularBookService;

  @InjectMocks
  private PopularBookBatchConfig batchConfig;

  @Mock
  private BatchMetricsService batchMetricsService;

  @Test
  @DisplayName("인기 도서 Tasklet이 실행되면 Service를 호출하고 FINISHED 상태를 반환한다")
  void testCalculatePopularBookTasklet() throws Exception {
    // given
    Tasklet tasklet = batchConfig.calculatePopularBookTasklet();
    StepContribution contribution = mock(StepContribution.class);
    ChunkContext chunkContext = mock(ChunkContext.class);

    // when
    RepeatStatus status = tasklet.execute(contribution, chunkContext);

    // then
    // Service의 랭킹 계산 로직이 정상적으로 1번 호출되었는지 검증
    verify(popularBookService, times(1)).calculatePopularBook(any(LocalDate.class));
    // Tasklet이 무사히 종료(FINISHED)되었는지 검증
    assertEquals(RepeatStatus.FINISHED, status);
  }
}