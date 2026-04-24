package com.sb09.deokhugam.domain.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sb09.deokhugam.domain.book.dto.BookDto;
import com.sb09.deokhugam.domain.book.dto.NaverBookDto;
import com.sb09.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sb09.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.sb09.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.sb09.deokhugam.domain.book.entity.Book;
import com.sb09.deokhugam.domain.book.mapper.BookMapper;
import com.sb09.deokhugam.domain.book.repository.BookRepository;
import com.sb09.deokhugam.domain.book.service.Basic.BasicBookService;
import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.domain.dashboard.entity.PopularBook;
import com.sb09.deokhugam.domain.dashboard.repository.PopularBookRepository;
import com.sb09.deokhugam.global.exception.CustomException;
import com.sb09.deokhugam.global.exception.ErrorCode;
import com.sb09.deokhugam.global.exception.book.BookNotFoundException;
import com.sb09.deokhugam.global.exception.book.DuplicateIsbnException;
import com.sb09.deokhugam.global.common.mapper.CursorPageResponseMapper;
import com.sb09.deokhugam.global.infrastructure.NaverBookClient;
import com.sb09.deokhugam.global.infrastructure.OcrClient;
import com.sb09.deokhugam.global.infrastructure.S3Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BasicBookServiceTest {

  @Mock
  private BookRepository bookRepository;
  @Mock
  private BookMapper bookMapper;
  @Mock
  private NaverBookClient naverBookClient;
  @Mock
  private OcrClient ocrClient;
  @Mock
  private S3Service s3Service;
  @Mock
  private CursorPageResponseMapper cursorPageResponseMapper;
  @Mock
  private PopularBookRepository popularBookRepository;

  @InjectMocks
  private BasicBookService bookService;

  private UUID bookId;
  private Book book;

  @BeforeEach
  void setUp() {
    bookId = UUID.randomUUID();
    book = mock(Book.class);
    given(book.getId()).willReturn(bookId);
    given(book.getDeletedAt()).willReturn(null);
  }

  // ===================== CREATE =====================

  @Test
  @DisplayName("도서 등록 성공 - 썸네일 없음")
  void create_success_noThumbnail() {
    BookCreateRequest request = new BookCreateRequest(
        "테스트 도서", "저자명", "도서 설명", "출판사", LocalDate.of(2024, 1, 1), "9791140712496"
    );
    given(bookRepository.existsByIsbnAndDeletedAtIsNull(request.isbn())).willReturn(false);
    given(bookRepository.save(any(Book.class))).willReturn(book);
    given(bookMapper.toDto(book)).willReturn(mock(BookDto.class));

    bookService.create(request, null);

    verify(bookRepository, times(1)).save(any(Book.class));
    verify(s3Service, never()).upload(any());
  }

  @Test
  @DisplayName("도서 등록 성공 - 썸네일 있음 (S3 업로드 경로 검증)")
  void create_success_withThumbnail() {
    BookCreateRequest request = new BookCreateRequest(
        "테스트 도서", "저자명", "도서 설명", "출판사", LocalDate.of(2024, 1, 1), "9791140712496"
    );
    MultipartFile thumbnail = mock(MultipartFile.class);
    given(thumbnail.isEmpty()).willReturn(false);
    given(bookRepository.existsByIsbnAndDeletedAtIsNull(request.isbn())).willReturn(false);
    given(s3Service.upload(thumbnail)).willReturn("https://s3.example.com/thumb.jpg");
    given(bookRepository.save(any(Book.class))).willReturn(book);
    given(bookMapper.toDto(book)).willReturn(mock(BookDto.class));

    bookService.create(request, thumbnail);

    verify(s3Service, times(1)).upload(thumbnail);
    verify(bookRepository, times(1)).save(any(Book.class));
  }

  @Test
  @DisplayName("예외 검증 - ISBN 중복 시 DuplicateIsbnException 발생")
  void create_duplicateIsbn() {
    BookCreateRequest request = new BookCreateRequest(
        "테스트 도서", "저자명", "도서 설명", "출판사", LocalDate.of(2024, 1, 1), "9791140712496"
    );
    given(bookRepository.existsByIsbnAndDeletedAtIsNull(request.isbn())).willReturn(true);

    assertThatThrownBy(() -> bookService.create(request, null))
        .isInstanceOf(DuplicateIsbnException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.DUPLICATE_ISBN));
  }

  // ===================== FIND BY ID =====================

  @Test
  @DisplayName("도서 단건 조회 성공")
  void findById_success() {
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(bookMapper.toDto(book)).willReturn(mock(BookDto.class));

    bookService.findById(bookId);

    verify(bookRepository, times(1)).findById(bookId);
  }

  @Test
  @DisplayName("예외 검증 - 존재하지 않는 도서 조회 시 BookNotFoundException 발생")
  void findById_notFound() {
    given(bookRepository.findById(bookId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.findById(bookId))
        .isInstanceOf(BookNotFoundException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.BOOK_NOT_FOUND));
  }

  @Test
  @DisplayName("예외 검증 - 논리 삭제된 도서 조회 시 BookNotFoundException 발생")
  void findById_alreadyDeleted() {
    given(book.getDeletedAt()).willReturn(LocalDateTime.now());
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));

    assertThatThrownBy(() -> bookService.findById(bookId))
        .isInstanceOf(BookNotFoundException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.BOOK_NOT_FOUND));
  }

  // ===================== UPDATE =====================

  @Test
  @DisplayName("도서 수정 성공 - 썸네일 없음")
  void update_success_noThumbnail() {
    BookUpdateRequest request = new BookUpdateRequest(
        "수정된 제목", "수정된 저자", "수정된 설명", "수정된 출판사", LocalDate.of(2024, 6, 1)
    );
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(bookMapper.toDto(book)).willReturn(mock(BookDto.class));

    bookService.update(bookId, request, null);

    verify(book, times(1)).update(any(), any(), any(), any(), any(), any());
    verify(s3Service, never()).upload(any());
  }

  @Test
  @DisplayName("도서 수정 성공 - 썸네일 있음 (S3 업로드 경로 검증)")
  void update_success_withThumbnail() {
    BookUpdateRequest request = new BookUpdateRequest(
        "수정된 제목", null, null, null, null
    );
    MultipartFile thumbnail = mock(MultipartFile.class);
    given(thumbnail.isEmpty()).willReturn(false);
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(s3Service.upload(thumbnail)).willReturn("https://s3.example.com/new.jpg");
    given(bookMapper.toDto(book)).willReturn(mock(BookDto.class));

    bookService.update(bookId, request, thumbnail);

    verify(s3Service, times(1)).upload(thumbnail);
  }

  @Test
  @DisplayName("예외 검증 - 존재하지 않는 도서 수정 시 BookNotFoundException 발생")
  void update_notFound() {
    given(bookRepository.findById(bookId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.update(bookId, null, null))
        .isInstanceOf(BookNotFoundException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.BOOK_NOT_FOUND));
  }

  @Test
  @DisplayName("예외 검증 - 논리 삭제된 도서 수정 시 BookNotFoundException 발생")
  void update_alreadyDeleted() {
    given(book.getDeletedAt()).willReturn(LocalDateTime.now());
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));

    assertThatThrownBy(() -> bookService.update(bookId, null, null))
        .isInstanceOf(BookNotFoundException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.BOOK_NOT_FOUND));
  }

  // ===================== SOFT DELETE =====================

  @Test
  @DisplayName("도서 논리 삭제 성공")
  void softDelete_success() {
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));

    bookService.softDelete(bookId);

    verify(book, times(1)).markAsDeleted();
  }

  @Test
  @DisplayName("예외 검증 - 존재하지 않는 도서 논리 삭제 시 BookNotFoundException 발생")
  void softDelete_notFound() {
    given(bookRepository.findById(bookId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> bookService.softDelete(bookId))
        .isInstanceOf(BookNotFoundException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.BOOK_NOT_FOUND));
  }

  @Test
  @DisplayName("예외 검증 - 이미 삭제된 도서 재삭제 시 BookNotFoundException 발생")
  void softDelete_alreadyDeleted() {
    given(book.getDeletedAt()).willReturn(LocalDateTime.now());
    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));

    assertThatThrownBy(() -> bookService.softDelete(bookId))
        .isInstanceOf(BookNotFoundException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.BOOK_NOT_FOUND));
  }

  // ===================== HARD DELETE =====================

  @Test
  @DisplayName("도서 물리 삭제 성공")
  void hardDelete_success() {
    given(bookRepository.existsById(bookId)).willReturn(true);

    bookService.hardDelete(bookId);

    verify(bookRepository, times(1)).hardDeleteById(bookId);
  }

  @Test
  @DisplayName("예외 검증 - 존재하지 않는 도서 물리 삭제 시 BookNotFoundException 발생")
  void hardDelete_notFound() {
    given(bookRepository.existsById(bookId)).willReturn(false);

    assertThatThrownBy(() -> bookService.hardDelete(bookId))
        .isInstanceOf(BookNotFoundException.class)
        .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
            .isEqualTo(ErrorCode.BOOK_NOT_FOUND));
  }

  // ===================== NAVER / OCR =====================

  @Test
  @DisplayName("네이버 API ISBN 도서 정보 조회 성공")
  void getBookInfoByIsbn_success() {
    String isbn = "9791140712496";
    given(naverBookClient.searchByIsbn(isbn)).willReturn(mock(NaverBookDto.class));

    bookService.getBookInfoByIsbn(isbn);

    verify(naverBookClient, times(1)).searchByIsbn(isbn);
  }

  @Test
  @DisplayName("OCR ISBN 추출 성공")
  void getIsbnByImage_success() {
    MultipartFile image = mock(MultipartFile.class);
    given(ocrClient.extractIsbn(image)).willReturn("9791140712496");

    String result = bookService.getIsbnByImage(image);

    assertThat(result).isEqualTo("9791140712496");
    verify(ocrClient, times(1)).extractIsbn(image);
  }

  // ===================== GET BOOKS =====================

  @Test
  @DisplayName("도서 목록 검색 성공")
  void getBooks_success() {
    BookSearchCondition condition = new BookSearchCondition(
        null, "createdAt", "DESC", null, null, 20
    );
    SliceImpl<Book> slice = new SliceImpl<>(
        List.of(book), PageRequest.of(0, 20), false
    );

    given(bookRepository.searchBooks(condition)).willReturn(slice);

    bookService.getBooks(condition);

    verify(bookRepository, times(1)).searchBooks(condition);
  }

  // ===================== GET POPULAR BOOKS =====================

  @Test
  @DisplayName("인기 도서 조회 성공")
  void getPopularBooks_success() {
    PopularBook popularBook = mock(PopularBook.class);
    LocalDate baseDate = LocalDate.of(2024, 1, 1);

    given(popularBook.getBookId()).willReturn(bookId);
    given(popularBook.getPeriod()).willReturn(PeriodType.DAILY);
    given(popularBook.getBaseDate()).willReturn(baseDate);
    given(popularBook.getRanking()).willReturn(1L);
    given(popularBook.getScore()).willReturn(BigDecimal.valueOf(9.5));
    given(popularBook.getReviewCount()).willReturn(10L);
    given(popularBook.getRating()).willReturn(BigDecimal.valueOf(4.5));
    given(popularBook.getCreatedAt()).willReturn(LocalDateTime.now());
    given(popularBook.getId()).willReturn(UUID.randomUUID());

    given(popularBookRepository.findTopByPeriodOrderByBaseDateDesc(PeriodType.DAILY))
        .willReturn(Optional.of(popularBook));
    given(popularBookRepository.findAll()).willReturn(List.of(popularBook));
    given(bookRepository.findAllById(anyList())).willReturn(List.of(book));
    given(book.getTitle()).willReturn("테스트 도서");
    given(book.getAuthor()).willReturn("저자");
    given(book.getThumbnailUrl()).willReturn(null);

    bookService.getPopularBooks(PeriodType.DAILY, null, null, 20);

    verify(popularBookRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("인기 도서 조회 - 배치 데이터 없을 시 빈 목록 반환")
  void getPopularBooks_noData() {
    given(popularBookRepository.findTopByPeriodOrderByBaseDateDesc(PeriodType.WEEKLY))
        .willReturn(Optional.empty());
    given(popularBookRepository.findAll()).willReturn(List.of());

    bookService.getPopularBooks(PeriodType.WEEKLY, null, null, 20);

    verify(popularBookRepository, times(1)).findAll();
  }
}