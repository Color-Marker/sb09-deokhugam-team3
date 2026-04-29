package com.sb09.deokhugam.domain.dashboard.service.basic;



import com.sb09.deokhugam.domain.book.repository.BookRepository;
import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.domain.dashboard.entity.PopularBook;
import com.sb09.deokhugam.domain.dashboard.repository.PopularBookRepository;
import com.sb09.deokhugam.domain.dashboard.service.PopularBookService;
import com.sb09.deokhugam.domain.review.repository.ReviewRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicPopularBookService implements PopularBookService {

  private final PopularBookRepository popularBookRepository;
  private final ReviewRepository reviewRepository;
  private final BookRepository bookRepository;

  @Transactional
  @Override
  public void calculatePopularBook(LocalDate baseDate) {

    for(PeriodType period : PeriodType.values()){
      popularBookRepository.deleteByPeriodAndBaseDate(period, baseDate);

        LocalDateTime from = calculateFrom(period, baseDate);
        LocalDateTime to = baseDate.atStartOfDay();

        List<Object[]> results = reviewRepository.calculateBookByPeriod(from, to);

        long rank = 1;
        for(Object[] row : results){
          UUID bookId = (UUID) row[0];
          if(bookRepository.existsByIdAndDeletedAtIsNotNull(bookId)){
            continue;
          }
          Long reviewCount = (Long) row[1];
          BigDecimal avgRating = BigDecimal.valueOf((Double) row[2]);
          BigDecimal score = BigDecimal.valueOf(reviewCount).multiply(new BigDecimal("0.4"))
              .add(avgRating.multiply(new BigDecimal("0.6")));
          popularBookRepository.save(
              PopularBook.builder()
                  .bookId(bookId)
                  .period(period)
                  .baseDate(baseDate)
                  .ranking(rank++)
                  .score(score)
                  .reviewCount(reviewCount)
                  .rating(avgRating)
              .build());
        }
    }
  }

  private LocalDateTime calculateFrom(PeriodType period, LocalDate baseDate){
    return switch (period) {
      case DAILY -> baseDate.minusDays(1).atStartOfDay();
      case WEEKLY -> baseDate.minusWeeks(1).atStartOfDay();
      case MONTHLY -> baseDate.minusMonths(1).atStartOfDay();
      case ALL_TIME -> LocalDateTime.of(2000, 1, 1, 0, 0);
    };
  }
}
