package com.sb09.deokhugam.domain.book.service;

import com.sb09.deokhugam.domain.book.dto.BookDto;
import com.sb09.deokhugam.domain.book.dto.NaverBookDto;
import com.sb09.deokhugam.domain.book.dto.PopularBookDto;
import com.sb09.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sb09.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.sb09.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {

  //도서 등록
  BookDto create(BookCreateRequest request, MultipartFile thumbnailImage);

  //도서 단건 조회(도서 상세 정보 조회)
  BookDto findById(UUID bookId);

  //도서 수정
  BookDto update(UUID bookId, BookUpdateRequest request, MultipartFile thumbnailImage);

  //도서 논리 삭제
  void softDelete(UUID bookId);

  //도서 물리 삭제
  void hardDelete(UUID bookId);

  //Naver API - ISBN으로 도서 정보 조회
  NaverBookDto getBookInfoByIsbn(String isbn);

  //OCR - 이미지에서 ISBN 인식
  String getIsbnByImage(MultipartFile image);

  // 도서 목록 조회 (키워드 검색 + 커서 페이지네이션)
  CursorPageResponseDto<BookDto> searchBooks(BookSearchCondition condition);

  //인기 도서 목록 조회 (period 필터, ranking 순 커서 페이지네이션)
  CursorPageResponseDto<PopularBookDto> getPopularBooks(
      PeriodType period,
      Long cursor,
      LocalDateTime after,
      int limit
  );

}
