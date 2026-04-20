package com.sb09.deokhugam.domain.book.repository.custom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb09.deokhugam.domain.book.dto.request.BookSearchCondition;
import com.sb09.deokhugam.domain.book.entity.Book;
import com.sb09.deokhugam.domain.book.entity.QBook;
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

    List<Book> results = queryFactory
        .selectFrom(book)
        .where(
            book.deletedAt.isNull(),
            keywordCondition(condition.keyword()),
            cursorCondition(condition.cursor(), condition.after(), isDesc)
        )
        .orderBy(
            isDesc ? book.createdAt.desc() : book.createdAt.asc(),
            isDesc ? book.id.desc() : book.id.asc()
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
        .or(book.author.containsIgnoreCase(keyword));
  }

  private BooleanExpression cursorCondition(Object cursor, LocalDateTime after, boolean isDesc) {
    if (cursor == null || after == null) {
      return null;
    }
    UUID cursorId = UUID.fromString(cursor.toString());
    if (isDesc) {
      return book.createdAt.lt(after)
          .or(book.createdAt.eq(after).and(book.id.lt(cursorId)));
    } else {
      return book.createdAt.gt(after)
          .or(book.createdAt.eq(after).and(book.id.gt(cursorId)));
    }
  }
}