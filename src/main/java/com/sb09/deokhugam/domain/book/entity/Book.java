package com.sb09.deokhugam.domain.book.entity;

import com.sb09.deokhugam.global.common.entity.BaseFullAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseFullAuditEntity {

  @Column(name = "title", nullable = false, length = 500)
  private String title;

  @Column(name = "author", nullable = false, length = 255)
  private String author;

  @Column(name = "description", nullable = false, columnDefinition = "TEXT")
  private String description;

  @Column(name = "publisher", nullable = false, length = 255)
  private String publisher;

  @Column(name = "published_date", nullable = false)
  private LocalDate publishedDate;

  @Column(name = "isbn", length = 20)
  private String isbn;

  @Column(name = "thumbnail_url", length = 2048)
  private String thumbnailUrl;

  @Column(name = "review_count", nullable = false)
  private int reviewCount = 0;

  @Column(name = "rating", nullable = false, precision = 3, scale = 2)
  private BigDecimal rating = BigDecimal.ZERO;

  public Book(String title, String author, String description, String publisher,
      LocalDate publishedDate,
      String isbn, String thumbnailUrl) {
    this.title = title;
    this.author = author;
    this.description = description;
    this.publisher = publisher;
    this.publishedDate = publishedDate;
    this.isbn = isbn;
    this.thumbnailUrl = thumbnailUrl;
  }

  public void update(String title, String author, String description, String publisher,
      LocalDate publishedDate, String thumbnailUrl) {
    if (title != null) {
      this.title = title;
    }
    if (author != null) {
      this.author = author;
    }
    if (description != null) {
      this.description = description;
    }
    if (publisher != null) {
      this.publisher = publisher;
    }
    if (publishedDate != null) {
      this.publishedDate = publishedDate;
    }
    if (thumbnailUrl != null) {
      this.thumbnailUrl = thumbnailUrl;
    }
  }

  public void updateRatingAndReviewCount(BigDecimal rating, int reviewCount) {
    this.rating = rating;
    this.reviewCount = reviewCount;
  }

  public void removeReviewStat(Integer deletedRating) {
    int newReviewCount = this.reviewCount - 1;
    double newAverage = 0.0;
    if (newReviewCount > 0) {
      double currentTotal = this.rating.doubleValue() * this.reviewCount;
      newAverage = (currentTotal - deletedRating) / newReviewCount;
    }
    this.rating = BigDecimal.valueOf(newAverage).setScale(2, RoundingMode.HALF_UP);
    this.reviewCount = newReviewCount;
  }
}
