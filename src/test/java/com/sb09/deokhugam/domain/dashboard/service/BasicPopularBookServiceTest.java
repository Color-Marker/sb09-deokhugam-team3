package com.sb09.deokhugam.domain.dashboard.service;

import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.domain.dashboard.entity.PopularBook;
import com.sb09.deokhugam.domain.dashboard.repository.PopularBookRepository;
import com.sb09.deokhugam.domain.dashboard.service.basic.BasicPopularBookService;
import com.sb09.deokhugam.domain.review.repository.ReviewRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicPopularBookServiceTest {

  @InjectMocks
  private BasicPopularBookService popularBookService;

  @Mock
  private PopularBookRepository popularBookRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @Test
  @DisplayName("인기 도서 배치 로직이 리팩토링된 방식(삭제 후 삽입)으로 정상 수행된다")
  void calculatePopularBook_Success() {
    // given
    LocalDate baseDate = LocalDate.of(2026, 4, 27);
    List<Object[]> mockResults = new ArrayList<>();

    // 가짜 데이터 5개 생성 (팀원 코드에 맞게 Object 배열 형태: UUID, Long, Double 순서)
    for (int i = 0; i < 5; i++) {
      Object[] row = new Object[]{
          UUID.randomUUID(), // row[0]: bookId (UUID)
          10L + i,           // row[1]: reviewCount (Long)
          4.5                // row[2]: avgRating (Double)
      };
      mockResults.add(row);
    }

    // ReviewRepository의 실제 메서드(calculateBookByPeriod) 모킹
    when(reviewRepository.calculateBookByPeriod(any(), any()))
        .thenReturn(mockResults);

    // when
    popularBookService.calculatePopularBook(baseDate);

    // then
    // 1. 4가지 기간(DAILY, WEEKLY, MONTHLY, ALL_TIME)에 대해 삭제가 잘 호출되는가?
    verify(popularBookRepository, times(4)).deleteByPeriodAndBaseDate(any(PeriodType.class), eq(baseDate));

    // 2. (4가지 기간 * 5개의 가짜 데이터) = 총 20번의 save()가 정상적으로 호출되는가?
    verify(popularBookRepository, times(20)).save(any(PopularBook.class));
  }
}