package com.sb09.deokhugam.domain.dashboard.service.basic;


import com.sb09.deokhugam.domain.book.entity.Book;
import com.sb09.deokhugam.domain.book.repository.BookRepository;
import com.sb09.deokhugam.domain.dashboard.dto.request.PopularBookListRequest;
import com.sb09.deokhugam.domain.dashboard.dto.response.PopularBookDto;
import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.domain.dashboard.entity.PopularBook;
import com.sb09.deokhugam.domain.dashboard.mapper.PopularBookMapper;
import com.sb09.deokhugam.domain.dashboard.repository.PopularBookRepository;
import com.sb09.deokhugam.domain.dashboard.service.PopularBookService;
import com.sb09.deokhugam.domain.review.repository.ReviewRepository;
import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;
import com.sb09.deokhugam.global.Exception.user.UserNotFoundException;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import com.sb09.deokhugam.global.common.mapper.CursorPageResponseMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicPopularBookService implements PopularBookService {

  private final PopularBookRepository popularBookRepository;
  private final BookRepository bookRepository;
  private final ReviewRepository reviewRepository;
  private final CursorPageResponseMapper cursorPageResponseMapper;
  private final PopularBookMapper popularBookMapper;

  @Transactional(readOnly = true)
  @Override
  public CursorPageResponseDto<PopularBookDto> list(PopularBookListRequest request) {
    PageRequest pageable = PageRequest.of(0, request.getLimit());
    Slice<PopularBook> slice;
    Long cursor = (Long) request.getCursor();
    LocalDate baseDate = LocalDate.now();
    if (request.getDirection() == Sort.Direction.ASC) {
      slice = popularBookRepository.findPopularBooksAsc(request.getPeriod(), baseDate, cursor, request.getAfter(),
          pageable);
    } else {
      slice = popularBookRepository.findPopularBooksDesc(request.getPeriod(), baseDate, cursor, request.getAfter(),
          pageable);
    }
    Long totalElements = popularBookRepository.countByPeriod(request.getPeriod(), baseDate);
    log.info("{} 기간의 인기 도서 목록을 불러옵니다.", request.getPeriod().toString());
    log.info("인기 도서 {} 개를 불러옵니다.", totalElements);
    return cursorPageResponseMapper.fromSlice(
        slice,
        popularBookMapper::toDto,
        PopularBook::getRanking,
        PopularBook::getCreatedAt,
        totalElements
    );
  }

  @Override
  public void calculatePopularBook(LocalDate baseDate) {
    for(PeriodType period : PeriodType.values()){
        LocalDateTime from = calculateFrom(period, baseDate);
        LocalDateTime to = baseDate.atStartOfDay();

        List<Object[]> results = reviewRepository.calculateBookByPeriod(from, to);

        long rank = 1;
        for(Object[] row : results){
          UUID bookId = (UUID) row[0];
          Book book = bookRepository.findById(bookId).orElseThrow(
              () -> {
                log.warn("도서를 찾을 수 없습니다");
                return new CustomException(ErrorCode.BOOK_NOT_FOUND);
              }
          );
          if(book.getDeletedAt() != null){
            log.warn("도서를 찾을 수 없습니다");
            throw new CustomException(ErrorCode.BOOK_NOT_FOUND);
          }
          Long reviewCount = (Long) row[1];
          BigDecimal avgRating = BigDecimal.valueOf((Double) row[2]);
          BigDecimal score = BigDecimal.valueOf(reviewCount).multiply(new BigDecimal("0.4"))
              .add(avgRating.multiply(new BigDecimal("0.6")));
          popularBookRepository.save(
              PopularBook.builder()
                  .book(book)
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
