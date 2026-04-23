package com.sb09.deokhugam.domain.book.service.Basic;

import com.sb09.deokhugam.domain.book.dto.BookDto;
import com.sb09.deokhugam.domain.book.dto.NaverBookDto;
import com.sb09.deokhugam.domain.book.dto.PopularBookDto;
import com.sb09.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sb09.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.sb09.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.sb09.deokhugam.domain.book.entity.Book;
import com.sb09.deokhugam.domain.book.mapper.BookMapper;
import com.sb09.deokhugam.domain.book.repository.BookRepository;
import com.sb09.deokhugam.domain.dashboard.repository.PopularBookRepository;
import com.sb09.deokhugam.domain.book.service.BookService;
import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.domain.dashboard.entity.PopularBook;
import com.sb09.deokhugam.global.Exception.book.BookNotFoundException;
import com.sb09.deokhugam.global.Exception.book.DuplicateIsbnException;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import com.sb09.deokhugam.global.common.mapper.CursorPageResponseMapper;
import com.sb09.deokhugam.global.infrastructure.NaverBookClient;
import com.sb09.deokhugam.global.infrastructure.OcrClient;
import com.sb09.deokhugam.global.infrastructure.S3Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicBookService implements BookService {

  private final BookRepository bookRepository;
  private final BookMapper bookMapper;
  private final NaverBookClient naverBookClient;
  private final OcrClient ocrClient;
  private final S3Service s3Service;
  private final CursorPageResponseMapper cursorPageResponseMapper;
  private final PopularBookRepository popularBookRepository;

  @Override
  @Transactional
  public BookDto create(BookCreateRequest request, MultipartFile thumbnailImage) {
    // ISBN 중복 체크
    if (request.isbn() != null && bookRepository.existsByIsbnAndDeletedAtIsNull(request.isbn())) {
      throw DuplicateIsbnException.withIsbn(request.isbn());
    }

    // 썸네일 이미지 S3 업로드(오류 때문에 미리 작성해둔거, 나중에 수정할수도)
    String thumbnailUrl = null;
    if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
      thumbnailUrl = s3Service.upload(thumbnailImage);
    }

    // Entity 생성 및 저장
    Book book = new Book(
        request.title(),
        request.author(),
        request.description(),
        request.publisher(),
        request.publishedDate(),
        request.isbn(),
        thumbnailUrl
    );
    Book savedBook = bookRepository.save(book);
    log.info("도서 등록 완료: ID={}", savedBook.getId());

    return bookMapper.toDto(savedBook);
  }

  @Override
  @Transactional(readOnly = true)
  public BookDto findById(UUID bookId) {
    //
    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> BookNotFoundException.withId(bookId));

    // 논리 삭제된 도서 접근 차단
    if (book.getDeletedAt() != null) {
      throw BookNotFoundException.withId(bookId);
    }

    return bookMapper.toDto(book);
  }

  @Override
  @Transactional
  public BookDto update(UUID bookId, BookUpdateRequest request, MultipartFile thumbnailImage) {
    // 도서 조회
    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> BookNotFoundException.withId(bookId));

    // 논리 삭제된 도서 접근 차단
    if (book.getDeletedAt() != null) {
      throw BookNotFoundException.withId(bookId);
    }

    // 썸네일 이미지 업데이트
    String thumbnailUrl = book.getThumbnailUrl();
    if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
      thumbnailUrl = s3Service.upload(thumbnailImage);
    }

    // 도서 정보 수정
    book.update(
        request.title(),
        request.author(),
        request.description(),
        request.publisher(),
        request.publishedDate(),
        thumbnailUrl
    );
    log.info("도서 수정 완료: ID={}", bookId);

    return bookMapper.toDto(book);
  }

  @Override
  @Transactional
  public void softDelete(UUID bookId) {
    //
    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> BookNotFoundException.withId(bookId));

    // 이미 삭제된 도서 재삭제 차단
    if (book.getDeletedAt() != null) {
      throw BookNotFoundException.withId(bookId);
    }

    book.markAsDeleted();
    log.info("도서 논리 삭제 완료: ID={}", bookId);
  }

  @Override
  @Transactional
  public void hardDelete(UUID bookId) {
    if (!bookRepository.existsById(bookId)) {
      throw BookNotFoundException.withId(bookId);
    }
    bookRepository.hardDeleteById(bookId);
    log.info("도서 물리 삭제 완료: ID={}", bookId);
  }

  @Override
  public NaverBookDto getBookInfoByIsbn(String isbn) {
    return naverBookClient.searchByIsbn(isbn);
  }

  @Override
  public String getIsbnByImage(MultipartFile image) {
    return ocrClient.extractIsbn(image);
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponseDto<BookDto> getBooks(BookSearchCondition condition) {
    Slice<Book> slice = bookRepository.searchBooks(condition);
    return cursorPageResponseMapper.fromSlice(
        slice,
        bookMapper::toDto,
        book -> book.getId(),
        book -> book.getCreatedAt(),
        null
    );
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponseDto<PopularBookDto> getPopularBooks(
      PeriodType period, Long cursor, LocalDateTime after, int limit
  ) {
    // 해당 period의 가장 최신 base_date 조회
    LocalDate latestBaseDate = popularBookRepository
        .findTopByPeriodOrderByBaseDateDesc(period)
        .map(PopularBook::getBaseDate)
        .orElse(LocalDate.now());

    // 커서 조건 적용하여 목록 조회
    List<PopularBook> results = popularBookRepository
        .findAll()
        .stream()
        .filter(pb -> pb.getPeriod() == period)
        .filter(pb -> pb.getBaseDate().equals(latestBaseDate))
        .filter(pb -> cursor == null || pb.getRanking() > cursor)
        .sorted((a, b) -> Long.compare(a.getRanking(), b.getRanking()))
        .limit(limit + 1)
        .toList();

    boolean hasNext = results.size() > limit;
    List<PopularBook> content = hasNext ? results.subList(0, limit) : results;

    // bookId 목록 추출 후 Book 일괄 조회 (N+1 방지)
    List<UUID> bookIds = content.stream()
        .map(PopularBook::getBookId)
        .toList();

    Map<UUID, Book> bookMap = bookRepository.findAllById(bookIds)
        .stream()
        .collect(Collectors.toMap(Book::getId, b -> b));

    // DTO 매핑
    List<PopularBookDto> dtos = content.stream()
        .map(pb -> {
          Book foundBook = bookMap.get(pb.getBookId());
          return new PopularBookDto(
              pb.getId(),
              pb.getBookId(),
              foundBook != null ? foundBook.getTitle() : null,
              foundBook != null ? foundBook.getAuthor() : null,
              foundBook != null ? foundBook.getThumbnailUrl() : null,
              pb.getPeriod(),
              pb.getBaseDate(),
              pb.getRanking(),
              pb.getScore(),
              pb.getReviewCount(),
              pb.getRating(),
              pb.getCreatedAt()
          );
        })
        .toList();

    PopularBook lastItem = content.isEmpty() ? null : content.get(content.size() - 1);
    Object nextCursor = hasNext && lastItem != null ? lastItem.getRanking() : null;
    LocalDateTime nextAfter = hasNext && lastItem != null ? lastItem.getCreatedAt() : null;

    return new CursorPageResponseDto<>(dtos, nextCursor, nextAfter, dtos.size(), null, hasNext);
  }
}
