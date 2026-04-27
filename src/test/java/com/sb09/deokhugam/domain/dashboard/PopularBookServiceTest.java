package com.sb09.deokhugam.domain.dashboard;

import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.domain.dashboard.entity.PopularBook;
import com.sb09.deokhugam.domain.dashboard.repository.PopularBookRepository;
import com.sb09.deokhugam.domain.dashboard.service.basic.BasicPopularBookService;
import com.sb09.deokhugam.domain.review.repository.ReviewRepository;
import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PopularBookServiceTest {
  @Mock
  private PopularBookRepository popularBookRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @InjectMocks
  private BasicPopularBookService popularBookService;

  @Test
  @DisplayName("이미 배치가 수행된 날짜면 저장 없이 바로 리턴한다")
  void calculatePopularBook_alreadyExists_skip() {
    // given
    LocalDate baseDate = LocalDate.of(2024, 1, 1);
    given(popularBookRepository.existsByBaseDate(baseDate)).willReturn(true);

    // when
    popularBookService.calculatePopularBook(baseDate);

    // then
    verify(popularBookRepository, never()).save(any());
    verify(reviewRepository, never()).calculateBookByPeriod(any(), any());
  }

  @Test
  @DisplayName("리뷰가 없는 기간은 PopularBook을 저장하지 않는다")
  void calculatePopularBook_noReviews_nothingSaved() {
    // given
    LocalDate baseDate = LocalDate.of(2024, 1, 1);
    given(popularBookRepository.existsByBaseDate(baseDate)).willReturn(false);
    given(reviewRepository.calculateBookByPeriod(any(), any())).willReturn(Collections.emptyList());

    // when
    popularBookService.calculatePopularBook(baseDate);

    // then
    verify(popularBookRepository, never()).save(any());
  }

  @Test
  @DisplayName("정상적으로 PopularBook을 계산하고 저장한다")
  void calculatePopularBook_success() {
    // given
    LocalDate baseDate = LocalDate.of(2024, 1, 1);
    UUID bookId = UUID.randomUUID();

    Object[] row = new Object[]{bookId, 10L, 4.5};
    List<Object[]> results = new ArrayList<>();
    results.add(row);

    given(popularBookRepository.existsByBaseDate(baseDate)).willReturn(false);
    given(reviewRepository.calculateBookByPeriod(any(), any())).willReturn(results);

    // when
    popularBookService.calculatePopularBook(baseDate);

    // then
    // PeriodType 개수만큼 저장 (DAILY, WEEKLY, MONTHLY, ALL_TIME = 4번)
    verify(popularBookRepository, times(PeriodType.values().length)).save(any(PopularBook.class));
  }

  @Test
  @DisplayName("스코어 계산이 올바르게 된다 (reviewCount * 0.4 + avgRating * 0.6)")
  void calculatePopularBook_scoreCalculation_correct() {
    // given
    LocalDate baseDate = LocalDate.of(2024, 1, 1);
    UUID bookId = UUID.randomUUID();

    long reviewCount = 10L;
    double avgRating = 4.0;
    // score = 10 * 0.4 + 4.0 * 0.6 = 4.0 + 2.4 = 6.4
    BigDecimal expectedScore = new BigDecimal("6.4");


    Object[] row = new Object[]{bookId, reviewCount, avgRating};

    List<Object[]> results = new ArrayList<>();
    results.add(row);

    given(popularBookRepository.existsByBaseDate(baseDate)).willReturn(false);
    given(reviewRepository.calculateBookByPeriod(any(), any())).willReturn(results);
    ArgumentCaptor<PopularBook> captor = ArgumentCaptor.forClass(PopularBook.class);

    // when
    popularBookService.calculatePopularBook(baseDate);

    // then
    verify(popularBookRepository, atLeastOnce()).save(captor.capture());
    PopularBook saved = captor.getAllValues().get(0);
    assertThat(saved.getScore()).isEqualByComparingTo(expectedScore);
    assertThat(saved.getBookId()).isEqualTo(bookId);
    assertThat(saved.getRanking()).isEqualTo(1L);
  }

  @Test
  @DisplayName("여러 도서가 있을 때 랭킹이 순서대로 부여된다")
  void calculatePopularBook_multipleBooks_rankingAssignedInOrder() {
    // given
    LocalDate baseDate = LocalDate.of(2024, 1, 1);

    Object[] row1 = new Object[]{UUID.randomUUID(), 20L, 5.0};
    Object[] row2 = new Object[]{UUID.randomUUID(), 10L, 4.0};
    Object[] row3 = new Object[]{UUID.randomUUID(), 5L, 3.0};

    given(popularBookRepository.existsByBaseDate(baseDate)).willReturn(false);
    given(reviewRepository.calculateBookByPeriod(any(), any())).willReturn(List.of(row1, row2, row3));

    ArgumentCaptor<PopularBook> captor = ArgumentCaptor.forClass(PopularBook.class);

    // when
    popularBookService.calculatePopularBook(baseDate);

    // then
    verify(popularBookRepository, atLeast(3)).save(captor.capture());
    List<PopularBook> saved = captor.getAllValues();

    // 첫 번째 period(DAILY)의 저장 결과만 확인
    assertThat(saved.get(0).getRanking()).isEqualTo(1L);
    assertThat(saved.get(1).getRanking()).isEqualTo(2L);
    assertThat(saved.get(2).getRanking()).isEqualTo(3L);
  }

  @Test
  @DisplayName("ALL_TIME은 2000년 1월 1일부터 집계한다")
  void calculatePopularBook_allTimePeriod_fromYear2000() {
    // given
    LocalDate baseDate = LocalDate.of(2024, 1, 1);
    given(popularBookRepository.existsByBaseDate(baseDate)).willReturn(false);
    given(reviewRepository.calculateBookByPeriod(any(), any())).willReturn(Collections.emptyList());

    ArgumentCaptor<java.time.LocalDateTime> fromCaptor = ArgumentCaptor.forClass(java.time.LocalDateTime.class);

    // when
    popularBookService.calculatePopularBook(baseDate);

    // then
    verify(reviewRepository, atLeast(1)).calculateBookByPeriod(fromCaptor.capture(), any());
    boolean hasAllTimeFrom = fromCaptor.getAllValues().stream()
        .anyMatch(dt -> dt.getYear() == 2000 && dt.getMonthValue() == 1 && dt.getDayOfMonth() == 1);
    assertThat(hasAllTimeFrom).isTrue();
  }
}
