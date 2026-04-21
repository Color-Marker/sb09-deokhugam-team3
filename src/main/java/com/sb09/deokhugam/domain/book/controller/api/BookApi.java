package com.sb09.deokhugam.domain.book.controller.api;

import com.sb09.deokhugam.domain.book.dto.BookDto;
import com.sb09.deokhugam.domain.book.dto.NaverBookDto;
import com.sb09.deokhugam.domain.book.dto.PopularBookDto;
import com.sb09.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sb09.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "도서 관리", description = "도서 관련 API")
public interface BookApi {

  @Operation(summary = "도서 등록", description = "새로운 도서를 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "도서 등록 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "409", description = "ISBN 중복")
  })
  ResponseEntity<BookDto> createBook(
      @RequestPart BookCreateRequest bookData,
      @RequestPart(required = false) MultipartFile thumbnailImage
  );

  @Operation(summary = "도서 상세 정보 조회", description = "도서 ID로 상세 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "도서 정보 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음")
  })
  ResponseEntity<BookDto> getBook(
      @Parameter(description = "도서 ID") @PathVariable UUID bookId
  );

  @Operation(summary = "도서 정보 수정", description = "도서 정보를 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "도서 정보 수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음")
  })
  ResponseEntity<BookDto> updateBook(
      @Parameter(description = "도서 ID") @PathVariable UUID bookId,
      @RequestPart BookUpdateRequest bookData,
      @RequestPart(required = false) MultipartFile thumbnailImage
  );

  @Operation(summary = "도서 논리 삭제", description = "도서를 논리적으로 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "도서 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음")
  })
  ResponseEntity<Void> deleteBook(
      @Parameter(description = "도서 ID") @PathVariable UUID bookId
  );

  @Operation(summary = "도서 물리 삭제", description = "도서를 물리적으로 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "도서 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음")
  })
  ResponseEntity<Void> hardDeleteBook(
      @Parameter(description = "도서 ID") @PathVariable UUID bookId
  );

  @Operation(summary = "ISBN으로 도서 정보 조회", description = "Naver API를 통해 ISBN으로 도서 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "도서 정보 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 ISBN 형식"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음")
  })
  ResponseEntity<NaverBookDto> getBookInfoByIsbn(
      @Parameter(description = "ISBN 번호") @RequestParam String isbn
  );

  @Operation(summary = "OCR 기반 ISBN 인식", description = "OCR을 통해 이미지에서 ISBN을 인식합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "ISBN 인식 성공"),
      @ApiResponse(responseCode = "400", description = "OCR 인식 실패")
  })
  ResponseEntity<String> getIsbnByImage(
      @RequestPart MultipartFile image
  );

  @Operation(summary = "도서 목록 검색", description = "키워드 및 조건으로 도서 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "도서 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<CursorPageResponseDto<BookDto>> searchBooks(
      @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
      @Parameter(description = "정렬 기준 (createdAt)") @RequestParam(defaultValue = "createdAt") String orderBy,
      @Parameter(description = "정렬 방향 (ASC/DESC)") @RequestParam(defaultValue = "DESC") String direction,
      @Parameter(description = "커서 값") @RequestParam(required = false) String cursor,
      @Parameter(description = "커서 시간") @RequestParam(required = false) LocalDateTime after,
      @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int limit
  );

  @Operation(summary = "인기 도서 목록 조회", description = "기간별 인기 도서 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "인기 도서 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<CursorPageResponseDto<PopularBookDto>> getPopularBooks(
      @Parameter(description = "기간 (DAILY/WEEKLY/MONTHLY/ALL_TIME)") @RequestParam PeriodType period,
      @Parameter(description = "커서 값") @RequestParam(required = false) Long cursor,
      @Parameter(description = "커서 시간") @RequestParam(required = false) LocalDateTime after,
      @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int limit
  );
}