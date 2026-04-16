package com.sb09.deokhugam.domain.book.service.Basic;

import com.sb09.deokhugam.domain.book.dto.BookDto;
import com.sb09.deokhugam.domain.book.dto.NaverBookDto;
import com.sb09.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sb09.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.sb09.deokhugam.domain.book.entity.Book;
import com.sb09.deokhugam.domain.book.mapper.BookMapper;
import com.sb09.deokhugam.domain.book.repository.BookRepository;
import com.sb09.deokhugam.domain.book.service.BookService;
import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;
import com.sb09.deokhugam.global.infrastructure.NaverBookClient;
import com.sb09.deokhugam.global.infrastructure.OcrClient;
import com.sb09.deokhugam.global.infrastructure.S3Service;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @Override
  @Transactional
  public BookDto create(BookCreateRequest request, MultipartFile thumbnailImage) {
    // ISBN 중복 체크
    if (request.isbn() != null && bookRepository.existsByIsbn(request.isbn())) {
      throw new CustomException(ErrorCode.DUPLICATE_ISBN);
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
    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));
    return bookMapper.toDto(book);
  }

  @Override
  @Transactional
  public BookDto update(UUID bookId, BookUpdateRequest request, MultipartFile thumbnailImage) {
    // 도서 조회
    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

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
    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));
    book.markAsDeleted();
    log.info("도서 논리 삭제 완료: ID={}", bookId);
  }

  @Override
  @Transactional
  public void hardDelete(UUID bookId) {
    if (!bookRepository.existsById(bookId)) {
      throw new CustomException(ErrorCode.BOOK_NOT_FOUND);
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
}
