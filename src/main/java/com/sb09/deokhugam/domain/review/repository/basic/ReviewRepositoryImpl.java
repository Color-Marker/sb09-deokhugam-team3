package com.sb09.deokhugam.domain.review.repository.basic;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb09.deokhugam.domain.book.entity.QBook;
import com.sb09.deokhugam.domain.review.dto.request.ReviewListRequest;
import com.sb09.deokhugam.domain.review.dto.response.ReviewDto;
import com.sb09.deokhugam.domain.review.entity.QReview;
import com.sb09.deokhugam.domain.review.entity.QReviewLike;
import com.sb09.deokhugam.domain.review.repository.ReviewRepositoryCustom;
import com.sb09.deokhugam.domain.user.entity.QUsers;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private final QReview review = QReview.review;
  private final QBook book = QBook.book;
  private final QUsers users = QUsers.users;
  private final QReviewLike reviewLike = QReviewLike.reviewLike;

  @Override
  public Slice<ReviewDto> searchReviews(ReviewListRequest request, UUID requestUserId) {

    List<ReviewDto> results = queryFactory
        .select(Projections.constructor(ReviewDto.class,
            review.id,
            review.bookId,
            book.title,
            book.thumbnailUrl,
            review.userId,
            users.nickname,
            review.content,
            review.rating,
            review.likeCount,
            review.commentCount,

            requestUserId != null ?
                JPAExpressions.selectOne()
                    .from(reviewLike)
                    .where(reviewLike.review.id.eq(review.id)
                        .and(reviewLike.user.id.eq(requestUserId)))
                    .exists()
                : Expressions.asBoolean(false),

            review.createdAt,
            review.updatedAt
        ))
        .from(review)
        .leftJoin(book).on(review.bookId.eq(book.id))
        .leftJoin(users).on(review.userId.eq(users.id))
        .where(
            review.deletedAt.isNull(),
            bookIdEq(request.bookId()),
            userIdEq(request.userId()),
            keywordContains(request.keyword()),
            cursorCondition(request)
        )
        .orderBy(createOrderSpecifier(request)) // 동적 정렬 적용
        .limit(request.limit() + 1)
        .fetch();

    boolean hasNext = results.size() > request.limit();
    if (hasNext) {
      results.remove(request.limit().intValue());
    }

    return new SliceImpl<>(results, PageRequest.of(0, request.limit()), hasNext);
  }

  // --- 조건 메서드들 ---
  private BooleanExpression bookIdEq(UUID bookId) {
    return bookId != null ? review.bookId.eq(bookId) : null;
  }

  private BooleanExpression userIdEq(UUID userId) {
    return userId != null ? review.userId.eq(userId) : null;
  }

  private BooleanExpression keywordContains(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return null;
    }
    return review.content.containsIgnoreCase(keyword)
        .or(book.title.containsIgnoreCase(keyword))
        .or(users.nickname.containsIgnoreCase(keyword));
  }

  // ---  정렬 및 커서 동적 처리 (수정) ---
  private OrderSpecifier<?>[] createOrderSpecifier(ReviewListRequest request) {
    boolean isAsc = request.direction() == Sort.Direction.ASC;

    if ("RATING".equalsIgnoreCase(request.orderBy())) {
      return new OrderSpecifier[]{
          isAsc ? review.rating.asc() : review.rating.desc(),
          isAsc ? review.createdAt.asc() : review.createdAt.desc(),
          isAsc ? review.id.asc() : review.id.desc()
      };
    }

    // 기본값: 최신순 (LATEST)
    return new OrderSpecifier[]{
        isAsc ? review.createdAt.asc() : review.createdAt.desc(),
        isAsc ? review.id.asc() : review.id.desc() // 시간 방향과 ID 방향을 일치시켜야 꼬이지 않음
    };
  }

  private BooleanExpression cursorCondition(ReviewListRequest request) {
    if (request.cursor() == null || request.after() == null) {
      return null;
    }

    UUID cursorId = request.cursor();
    LocalDateTime afterTime = request.after();
    boolean isAsc = request.direction() == Sort.Direction.ASC;

    if ("RATING".equalsIgnoreCase(request.orderBy())) {
      return isAsc ? review.createdAt.gt(afterTime) : review.createdAt.lt(afterTime);
    }

    // 기본 최신순 커서 로직 (방향에 따라 완벽하게 대칭되도록 수정)
    if (isAsc) {
      // 오름차순(오래된 순): 시간이 더 미래이거나, 시간이 같으면 ID가 더 큰 것
      return review.createdAt.gt(afterTime)
          .or(review.createdAt.eq(afterTime).and(review.id.gt(cursorId)));
    } else {
      // 내림차순(최신순): 시간이 더 과거이거나, 시간이 같으면 ID가 더 작은 것
      return review.createdAt.lt(afterTime)
          .or(review.createdAt.eq(afterTime).and(review.id.lt(cursorId)));
    }
  }
}