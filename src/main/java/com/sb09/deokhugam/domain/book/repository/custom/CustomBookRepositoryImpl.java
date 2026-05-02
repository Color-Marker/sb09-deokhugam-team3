package com.sb09.deokhugam.domain.book.repository.custom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb09.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.sb09.deokhugam.domain.book.entity.Book;
import com.sb09.deokhugam.domain.book.entity.QBook;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomBookRepositoryImpl implements CustomBookRepository {

  private final JPAQueryFactory queryFactory;
  private final QBook book = QBook.book;

  @Override
  public Slice<Book> searchBooks(BookSearchCondition condition) {
    boolean isDesc = !"ASC".equalsIgnoreCase(condition.direction());
    boolean isCreatedAtSort =
        condition.orderBy() == null || "createdAt".equals(condition.orderBy());

    List<Book> results = queryFactory
        .selectFrom(book)
        .where(
            book.deletedAt.isNull(),
            keywordCondition(condition.keyword()),
            cursorCondition(condition.orderBy(), condition.cursor(), condition.after(), isDesc)
        )
        .orderBy(
            getOrderSpecifier(condition.orderBy(), isDesc),
            isDesc ? (isCreatedAtSort ? book.id.desc() : book.createdAt.desc())
                : (isCreatedAtSort ? book.id.asc() : book.createdAt.asc())
        )
        .limit(condition.limit() + 1)
        .fetch();

    boolean hasNext = results.size() > condition.limit();
    if (hasNext) {
      results.remove(condition.limit());
    }

    return new SliceImpl<>(
        results,
        PageRequest.of(0, condition.limit()),
        hasNext
    );
  }

  private BooleanExpression keywordCondition(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return null;
    }
    return book.title.containsIgnoreCase(keyword)
        .or(book.author.containsIgnoreCase(keyword))
        .or(book.isbn.containsIgnoreCase(keyword));
  }

  private com.querydsl.core.types.OrderSpecifier<?> getOrderSpecifier(String orderBy,
      boolean isDesc) {
    if (orderBy == null) {
      return isDesc ? book.createdAt.desc() : book.createdAt.asc();
    }
    return switch (orderBy) {
      case "title" -> isDesc ? book.title.desc() : book.title.asc();
      case "publishedDate" -> isDesc ? book.publishedDate.desc() : book.publishedDate.asc();
      case "rating" -> isDesc ? book.rating.desc() : book.rating.asc();
      case "reviewCount" -> isDesc ? book.reviewCount.desc() : book.reviewCount.asc();
      default -> isDesc ? book.createdAt.desc() : book.createdAt.asc();
    };
  }

  private BooleanExpression cursorCondition(String orderBy, Object cursor, LocalDateTime after,
      boolean isDesc) {
    if (cursor == null || after == null) {
      return null;
    }
    if (orderBy == null || "createdAt".equals(orderBy)) {
      UUID cursorId = UUID.fromString(cursor.toString());
      return isDesc
          ? book.createdAt.lt(after).or(book.createdAt.eq(after).and(book.id.lt(cursorId)))
          : book.createdAt.gt(after).or(book.createdAt.eq(after).and(book.id.gt(cursorId)));
    }
    return switch (orderBy) {
      case "title" -> {
        String val = cursor.toString();
        yield isDesc
            ? book.title.lt(val).or(book.title.eq(val).and(book.createdAt.lt(after)))
            : book.title.gt(val).or(book.title.eq(val).and(book.createdAt.gt(after)));
      }
      case "publishedDate" -> {
        LocalDate val = LocalDate.parse(cursor.toString());
        yield isDesc
            ? book.publishedDate.lt(val)
            .or(book.publishedDate.eq(val).and(book.createdAt.lt(after)))
            : book.publishedDate.gt(val)
                .or(book.publishedDate.eq(val).and(book.createdAt.gt(after)));
      }
      case "rating" -> {
        BigDecimal val = new BigDecimal(cursor.toString());
        yield isDesc
            ? book.rating.lt(val).or(book.rating.eq(val).and(book.createdAt.lt(after)))
            : book.rating.gt(val).or(book.rating.eq(val).and(book.createdAt.gt(after)));
      }
      case "reviewCount" -> {
        int val = Integer.parseInt(cursor.toString());
        yield isDesc
            ? book.reviewCount.lt(val).or(book.reviewCount.eq(val).and(book.createdAt.lt(after)))
            : book.reviewCount.gt(val).or(book.reviewCount.eq(val).and(book.createdAt.gt(after)));
      }
      default -> null;
    };
  }
}