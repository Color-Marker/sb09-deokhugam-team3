package com.sb09.deokhugam.domain.dashboard.service;

import com.sb09.deokhugam.domain.dashboard.dto.PowerUserScoreDto;
import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.domain.dashboard.repository.DashboardQueryRepository;
import com.sb09.deokhugam.domain.dashboard.repository.PowerUserRepository;
import com.sb09.deokhugam.domain.dashboard.service.basic.BasicPowerUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicPowerUserServiceTest {

  @InjectMocks
  private BasicPowerUserService powerUserService;

  @Mock
  private DashboardQueryRepository dashboardQueryRepository;

  @Mock
  private PowerUserRepository powerUserRepository;

  @Test
  @DisplayName("파워 유저 배치 로직이 정상적으로 수행된다 (삭제 -> 조회 -> 저장)")
  void calculatePowerUser_Success() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 4, 27);
    List<PowerUserScoreDto> mockDtos = new ArrayList<>();

    // 5명의 가짜 파워 유저 데이터 생성
    for (int i = 0; i < 5; i++) {
      mockDtos.add(new PowerUserScoreDto(UUID.randomUUID(), 50.0, 20L, 10L));
    }

    // 레포지토리가 가짜 데이터를 반환하도록 설정
    when(dashboardQueryRepository.findTopPowerUsers(any(), any(), eq(100)))
        .thenReturn(mockDtos);

    // when
    powerUserService.calculatePowerUser(baseDate);

    // then
    // 1. 4가지 기간(DAILY, WEEKLY, MONTHLY, ALL_TIME)에 대해 삭제 로직이 호출되었는가?
    verify(powerUserRepository, times(4)).deleteByPeriodAndBaseDate(any(PeriodType.class), eq(baseDate));

    // 2. 4가지 기간에 대해 저장 로직이 호출되었는가?
    verify(powerUserRepository, times(4)).saveAll(anyList());
  }
}