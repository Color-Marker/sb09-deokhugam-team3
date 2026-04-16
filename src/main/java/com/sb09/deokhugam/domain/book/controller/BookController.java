package com.sb09.deokhugam.domain.book.controller;

import com.sb09.deokhugam.domain.book.controller.api.BookApi;
import com.sb09.deokhugam.domain.book.dto.BookDto;
import com.sb09.deokhugam.domain.book.dto.NaverBookDto;
import com.sb09.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sb09.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.sb09.deokhugam.domain.book.service.BookService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController implements BookApi {

  private final BookService bookService;

  @PostMapping(consumes = "multipart/form-data")
  public ResponseEntity<BookDto> createBook(
      @RequestPart @Valid BookCreateRequest bookData,
      @RequestPart(required = false) MultipartFile thumbnailImage
  ) {
    BookDto book = bookService.create(bookData, thumbnailImage);
    return ResponseEntity.status(HttpStatus.CREATED).body(book);
  }

  @GetMapping("/{bookId}")
  public ResponseEntity<BookDto> getBook(
      @PathVariable UUID bookId
  ) {
    BookDto book = bookService.findById(bookId);
    return ResponseEntity.ok(book);
  }

  @PatchMapping(value = "/{bookId}", consumes = "multipart/form-data")
  public ResponseEntity<BookDto> updateBook(
      @PathVariable UUID bookId,
      @RequestPart @Valid BookUpdateRequest bookData,
      @RequestPart(required = false) MultipartFile thumbnailImage
  ) {
    BookDto book = bookService.update(bookId, bookData, thumbnailImage);
    return ResponseEntity.ok(book);
  }

  @DeleteMapping("/{bookId}")
  public ResponseEntity<Void> deleteBook(
      @PathVariable UUID bookId
  ) {
    bookService.softDelete(bookId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{bookId}/hard")
  public ResponseEntity<Void> hardDeleteBook(
      @PathVariable UUID bookId
  ) {
    bookService.hardDelete(bookId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/info")
  public ResponseEntity<NaverBookDto> getBookInfoByIsbn(
      @RequestParam String isbn
  ) {
    NaverBookDto bookInfo = bookService.getBookInfoByIsbn(isbn);
    return ResponseEntity.ok(bookInfo);
  }

  @PostMapping(value = "/isbn/ocr", consumes = "multipart/form-data")
  public ResponseEntity<String> getIsbnByImage(
      @RequestPart MultipartFile image
  ) {
    String isbn = bookService.getIsbnByImage(image);
    return ResponseEntity.ok(isbn);
  }
}