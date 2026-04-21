package com.sb09.deokhugam.domain.book.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.deokhugam.domain.book.dto.BookDto;
import com.sb09.deokhugam.domain.book.dto.NaverBookDto;
import com.sb09.deokhugam.domain.book.dto.PopularBookDto;
import com.sb09.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sb09.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.sb09.deokhugam.domain.book.service.BookService;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
class BookControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private BookService bookService;

  // ===================== CREATE =====================

  @Test
  @DisplayName("도서 등록 API - 성공 시 201 Created 반환")
  void createBook_success() throws Exception {
    UUID bookId = UUID.randomUUID();
    BookCreateRequest request = new BookCreateRequest(
        "테스트 도서", "저자명", "도서 설명", "출판사", LocalDate.of(2024, 1, 1), "9791140712496"
    );
    BookDto response = new BookDto(
        bookId, "테스트 도서", "저자명", "도서 설명", "출판사",
        LocalDate.of(2024, 1, 1), "9791140712496", null,
        0, BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now()
    );

    given(bookService.create(any(), any())).willReturn(response);

    MockMultipartFile bookDataPart = new MockMultipartFile(
        "bookData", "", "application/json",
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(multipart("/api/books")
            .file(bookDataPart))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("테스트 도서"));
  }

  @Test
  @DisplayName("도서 등록 API - 제목 없을 시 400 Bad Request 반환")
  void createBook_validationFail() throws Exception {
    BookCreateRequest request = new BookCreateRequest(
        "", "저자명", "도서 설명", "출판사", LocalDate.of(2024, 1, 1), null
    );

    MockMultipartFile bookDataPart = new MockMultipartFile(
        "bookData", "", "application/json",
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(multipart("/api/books")
            .file(bookDataPart))
        .andExpect(status().isBadRequest());
  }

  // ===================== GET =====================

  @Test
  @DisplayName("도서 단건 조회 API - 성공 시 200 OK 반환")
  void getBook_success() throws Exception {
    UUID bookId = UUID.randomUUID();
    BookDto response = new BookDto(
        bookId, "테스트 도서", "저자명", "도서 설명", "출판사",
        LocalDate.of(2024, 1, 1), "9791140712496", null,
        0, BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now()
    );

    given(bookService.findById(eq(bookId))).willReturn(response);

    mockMvc.perform(get("/api/books/{bookId}", bookId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(bookId.toString()))
        .andExpect(jsonPath("$.title").value("테스트 도서"));
  }

  // ===================== UPDATE =====================

  @Test
  @DisplayName("도서 수정 API - 성공 시 200 OK 반환")
  void updateBook_success() throws Exception {
    UUID bookId = UUID.randomUUID();
    BookUpdateRequest request = new BookUpdateRequest(
        "수정된 제목", null, null, null, null
    );
    BookDto response = new BookDto(
        bookId, "수정된 제목", "저자명", "도서 설명", "출판사",
        LocalDate.of(2024, 1, 1), null, null,
        0, BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now()
    );

    given(bookService.update(eq(bookId), any(), any())).willReturn(response);

    MockMultipartFile bookDataPart = new MockMultipartFile(
        "bookData", "", "application/json",
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(multipart(HttpMethod.PATCH, "/api/books/{bookId}", bookId)
            .file(bookDataPart))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("수정된 제목"));
  }

  // ===================== DELETE =====================

  @Test
  @DisplayName("도서 논리 삭제 API - 성공 시 204 No Content 반환")
  void deleteBook_success() throws Exception {
    UUID bookId = UUID.randomUUID();

    mockMvc.perform(delete("/api/books/{bookId}", bookId))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("도서 물리 삭제 API - 성공 시 204 No Content 반환")
  void hardDeleteBook_success() throws Exception {
    UUID bookId = UUID.randomUUID();

    mockMvc.perform(delete("/api/books/{bookId}/hard", bookId))
        .andExpect(status().isNoContent());
  }

  // ===================== NAVER / OCR =====================

  @Test
  @DisplayName("ISBN 도서 정보 조회 API - 성공 시 200 OK 반환")
  void getBookInfoByIsbn_success() throws Exception {
    String isbn = "9791140712496";
    NaverBookDto response = new NaverBookDto(
        "테스트 도서", "저자명", "도서 설명", "출판사",
        LocalDate.of(2024, 1, 1), isbn, null
    );

    given(bookService.getBookInfoByIsbn(isbn)).willReturn(response);

    mockMvc.perform(get("/api/books/info")
            .param("isbn", isbn))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isbn").value(isbn));
  }

  @Test
  @DisplayName("OCR ISBN 추출 API - 성공 시 200 OK 반환")
  void getIsbnByImage_success() throws Exception {
    given(bookService.getIsbnByImage(any())).willReturn("9791140712496");

    MockMultipartFile image = new MockMultipartFile(
        "image", "test.jpg", "image/jpeg", "fake-image".getBytes()
    );

    mockMvc.perform(multipart("/api/books/isbn/ocr")
            .file(image))
        .andExpect(status().isOk());
  }

  // ===================== LIST / POPULAR =====================

  @Test
  @DisplayName("도서 목록 검색 API - 성공 시 200 OK 반환")
  void searchBooks_success() throws Exception {
    CursorPageResponseDto<BookDto> mockResponse = new CursorPageResponseDto<>(
        List.of(), null, null, 0, null, false
    );

    given(bookService.getBooks(any())).willReturn(mockResponse);

    mockMvc.perform(get("/api/books")
            .param("limit", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  @DisplayName("인기 도서 조회 API - 성공 시 200 OK 반환")
  void getPopularBooks_success() throws Exception {
    CursorPageResponseDto<PopularBookDto> mockResponse = new CursorPageResponseDto<>(
        List.of(), null, null, 0, null, false
    );

    given(bookService.getPopularBooks(any(), any(), any(), anyInt()))
        .willReturn(mockResponse);

    mockMvc.perform(get("/api/books/popular")
            .param("period", "DAILY")
            .param("limit", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }
}