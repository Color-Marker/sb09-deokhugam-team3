package com.sb09.deokhugam.domain.review.repository.basic;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb09.deokhugam.domain.review.dto.request.ReviewListRequest;
import com.sb09.deokhugam.domain.review.entity.QReview;
import com.sb09.deokhugam.domain.review.entity.Review;
import com.sb09.deokhugam.domain.review.repository.ReviewRepositoryCustom;
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
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

  // QueryDSL을 사용하기 위한 필수
  private final JPAQueryFactory queryFactory;
  private final QReview review = QReview.review;

  @Override
  public Slice<Review> searchReviews(ReviewListRequest request) {

    List<Review> results = queryFactory
        .selectFrom(review)
        .where(
            keywordContains(request.keyword()),
            cursorCondition(request)
        )
        .orderBy(review.createdAt.desc(), review.id.desc())
        .limit(request.limit() + 1) // .limit()사용
        .fetch();

    boolean hasNext = results.size() > request.limit();
    if (hasNext) {
      results.remove(request.limit().intValue());
    }

    return new SliceImpl<>(results, PageRequest.of(0, request.limit()), hasNext);
  }

  private BooleanExpression keywordContains(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return null;
    }
    return review.content.containsIgnoreCase(keyword);
  }

  private BooleanExpression cursorCondition(ReviewListRequest request) {
    if (request.cursor() == null || request.after() == null) { // .cursor(), .after() 사용
      return null;
    }

    UUID cursor = request.cursor();
    LocalDateTime after = request.after();

    return review.createdAt.lt(after)
        .or(review.createdAt.eq(after).and(review.id.lt(cursor)));
  }
}