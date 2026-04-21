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

  // ---  정렬 및 커서 동적 처리  ---
  private OrderSpecifier<?>[] createOrderSpecifier(ReviewListRequest request) {
    boolean isAsc = request.direction() == Sort.Direction.ASC;

    if ("RATING".equalsIgnoreCase(request.orderBy())) {
      return new OrderSpecifier[]{
          isAsc ? review.rating.asc() : review.rating.desc(),
          review.createdAt.desc()
      };
    }

    // 기본값: 최신순
    return new OrderSpecifier[]{
        isAsc ? review.createdAt.asc() : review.createdAt.desc(),
        review.id.desc()
    };
  }

  private BooleanExpression cursorCondition(ReviewListRequest request) {
    if (request.cursor() == null || request.after() == null) {
      return null;
    }

    UUID cursorId = request.cursor();
    LocalDateTime afterTime = request.after();

    //  DTO에서 rating 필드를 삭제했으므로, RATING 기반 커서 로직은
    // 단순 최신순(after) 기반으로 통합하거나 별도 처리가 필요합니다.
    // 여기서는 가장 기본이 되는 생성일시(createdAt) 기준 커서 방식을 적용합니다.

    if ("RATING".equalsIgnoreCase(request.orderBy())) {
      // 평점 정렬 시에도 생성일시 커서를 활용 (평점 데이터가 DTO에 없으므로)
      return review.createdAt.lt(afterTime);
    }

    // 기본 최신순 커서: 날짜가 더 이전이거나, 날짜가 같으면 ID가 더 작은 것
    return review.createdAt.lt(afterTime)
        .or(review.createdAt.eq(afterTime).and(review.id.lt(cursorId)));
  }
}