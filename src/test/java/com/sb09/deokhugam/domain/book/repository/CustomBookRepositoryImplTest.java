package com.sb09.deokhugam.domain.book.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb09.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.sb09.deokhugam.domain.book.entity.Book;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class CustomBookRepositoryImplTest {

  @TestConfiguration
  @EnableJpaAuditing
  static class TestConfig {

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em) {
      return new JPAQueryFactory(em);
    }
  }

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private EntityManager em;

  private Book alpha, beta, gamma;

  @BeforeEach
  void setUp() {
    alpha = bookRepository.save(
        new Book("Alpha", "A", "desc", "pub", LocalDate.of(2020, 1, 1), "ISBN001", null));
    beta = bookRepository.save(
        new Book("Beta", "B", "desc", "pub", LocalDate.of(2021, 6, 1), "ISBN002", null));
    gamma = bookRepository.save(
        new Book("Gamma", "C", "desc", "pub", LocalDate.of(2022, 12, 1), "ISBN003", null));

    alpha.updateRatingAndReviewCount(new BigDecimal("1.00"), 10);
    beta.updateRatingAndReviewCount(new BigDecimal("3.00"), 20);
    gamma.updateRatingAndReviewCount(new BigDecimal("5.00"), 30);

    bookRepository.flush();
    em.clear();
  }

  // ─── title ───────────────────────────────────────────────────────────

  @Test
  void searchBooks_titleDesc_cursorCondition() {
    Slice<Book> first = bookRepository.searchBooks(
        new BookSearchCondition(null, "title", "DESC", null, null, 2));
    assertThat(first.getContent()).extracting(Book::getTitle)
        .containsExactly("Gamma", "Beta");
    assertThat(first.hasNext()).isTrue();

    Book last = first.getContent().get(1);
    Slice<Book> second = bookRepository.searchBooks(
        new BookSearchCondition(null, "title", "DESC", last.getTitle(), last.getCreatedAt(), 2));
    assertThat(second.getContent()).extracting(Book::getTitle)
        .containsExactly("Alpha");
    assertThat(second.hasNext()).isFalse();
  }

  @Test
  void searchBooks_titleAsc_cursorCondition() {
    Slice<Book> first = bookRepository.searchBooks(
        new BookSearchCondition(null, "title", "ASC", null, null, 2));
    assertThat(first.getContent()).extracting(Book::getTitle)
        .containsExactly("Alpha", "Beta");
    assertThat(first.hasNext()).isTrue();

    Book last = first.getContent().get(1);
    Slice<Book> second = bookRepository.searchBooks(
        new BookSearchCondition(null, "title", "ASC", last.getTitle(), last.getCreatedAt(), 2));
    assertThat(second.getContent()).extracting(Book::getTitle)
        .containsExactly("Gamma");
    assertThat(second.hasNext()).isFalse();
  }

  // ─── publishedDate ───────────────────────────────────────────────────

  @Test
  void searchBooks_publishedDateDesc_cursorCondition() {
    Slice<Book> first = bookRepository.searchBooks(
        new BookSearchCondition(null, "publishedDate", "DESC", null, null, 2));
    assertThat(first.getContent()).extracting(Book::getTitle)
        .containsExactly("Gamma", "Beta");

    Book last = first.getContent().get(1);
    Slice<Book> second = bookRepository.searchBooks(new BookSearchCondition(
        null, "publishedDate", "DESC",
        last.getPublishedDate().toString(), last.getCreatedAt(), 2));
    assertThat(second.getContent()).extracting(Book::getTitle)
        .containsExactly("Alpha");
  }

  @Test
  void searchBooks_publishedDateAsc_cursorCondition() {
    Slice<Book> first = bookRepository.searchBooks(
        new BookSearchCondition(null, "publishedDate", "ASC", null, null, 2));
    assertThat(first.getContent()).extracting(Book::getTitle)
        .containsExactly("Alpha", "Beta");

    Book last = first.getContent().get(1);
    Slice<Book> second = bookRepository.searchBooks(new BookSearchCondition(
        null, "publishedDate", "ASC",
        last.getPublishedDate().toString(), last.getCreatedAt(), 2));
    assertThat(second.getContent()).extracting(Book::getTitle)
        .containsExactly("Gamma");
  }

  // ─── rating ──────────────────────────────────────────────────────────

  @Test
  void searchBooks_ratingDesc_cursorCondition() {
    Slice<Book> first = bookRepository.searchBooks(
        new BookSearchCondition(null, "rating", "DESC", null, null, 2));
    assertThat(first.getContent()).extracting(Book::getTitle)
        .containsExactly("Gamma", "Beta");

    Book last = first.getContent().get(1);
    Slice<Book> second = bookRepository.searchBooks(new BookSearchCondition(
        null, "rating", "DESC",
        last.getRating().toPlainString(), last.getCreatedAt(), 2));
    assertThat(second.getContent()).extracting(Book::getTitle)
        .containsExactly("Alpha");
  }

  @Test
  void searchBooks_ratingAsc_cursorCondition() {
    Slice<Book> first = bookRepository.searchBooks(
        new BookSearchCondition(null, "rating", "ASC", null, null, 2));
    assertThat(first.getContent()).extracting(Book::getTitle)
        .containsExactly("Alpha", "Beta");

    Book last = first.getContent().get(1);
    Slice<Book> second = bookRepository.searchBooks(new BookSearchCondition(
        null, "rating", "ASC",
        last.getRating().toPlainString(), last.getCreatedAt(), 2));
    assertThat(second.getContent()).extracting(Book::getTitle)
        .containsExactly("Gamma");
  }

  // ─── reviewCount ─────────────────────────────────────────────────────

  @Test
  void searchBooks_reviewCountDesc_cursorCondition() {
    Slice<Book> first = bookRepository.searchBooks(
        new BookSearchCondition(null, "reviewCount", "DESC", null, null, 2));
    assertThat(first.getContent()).extracting(Book::getTitle)
        .containsExactly("Gamma", "Beta");

    Book last = first.getContent().get(1);
    Slice<Book> second = bookRepository.searchBooks(new BookSearchCondition(
        null, "reviewCount", "DESC",
        String.valueOf(last.getReviewCount()), last.getCreatedAt(), 2));
    assertThat(second.getContent()).extracting(Book::getTitle)
        .containsExactly("Alpha");
  }

  @Test
  void searchBooks_reviewCountAsc_cursorCondition() {
    Slice<Book> first = bookRepository.searchBooks(
        new BookSearchCondition(null, "reviewCount", "ASC", null, null, 2));
    assertThat(first.getContent()).extracting(Book::getTitle)
        .containsExactly("Alpha", "Beta");

    Book last = first.getContent().get(1);
    Slice<Book> second = bookRepository.searchBooks(new BookSearchCondition(
        null, "reviewCount", "ASC",
        String.valueOf(last.getReviewCount()), last.getCreatedAt(), 2));
    assertThat(second.getContent()).extracting(Book::getTitle)
        .containsExactly("Gamma");
  }
}