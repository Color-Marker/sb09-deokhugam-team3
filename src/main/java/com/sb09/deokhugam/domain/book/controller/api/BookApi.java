package com.sb09.deokhugam.domain.book.controller.api;

import com.sb09.deokhugam.domain.book.dto.BookDto;
import com.sb09.deokhugam.domain.book.dto.NaverBookDto;
import com.sb09.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sb09.deokhugam.domain.book.dto.request.BookUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
      @ApiResponse(responseCode = "404", description = "도서 정보 없음")
  })
  ResponseEntity<BookDto> getBook(
      @Parameter(description = "도서 ID") @PathVariable UUID bookId
  );

  @Operation(summary = "도서 정보 수정", description = "도서 정보를 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "도서 정보 수정 성공"),
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
}