package com.sb09.deokhugam.domain.dashboard.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb09.deokhugam.domain.comment.entity.QComment;
import com.sb09.deokhugam.domain.dashboard.dto.PopularReviewScoreDto;
import com.sb09.deokhugam.domain.dashboard.dto.PowerUserScoreDto;
import com.sb09.deokhugam.domain.review.entity.QReview;
import com.sb09.deokhugam.domain.review.entity.QReviewLike;
import com.sb09.deokhugam.domain.user.entity.QUsers;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DashboardQueryRepository {

  private final JPAQueryFactory queryFactory;

  /**
   * 1. 인기 리뷰 통계 계산 쿼리
   */
  public List<PopularReviewScoreDto> findTopPopularReviews(LocalDateTime start, LocalDateTime end, int limitSize) {
    QReview review = QReview.review;
    QReviewLike reviewLike = QReviewLike.reviewLike;
    QComment comment = QComment.comment;

    // 에러 해결: Expressions.asNumber()로 서브쿼리를 숫자 표현식으로 감싸줌
    NumberExpression<Long> periodLikes = Expressions.asNumber(
        JPAExpressions.select(reviewLike.count())
            .from(reviewLike)
            .where(reviewLike.review.eq(review)
                .and(reviewLike.createdAt.between(start, end)))
    );

    NumberExpression<Long> periodComments = Expressions.asNumber(
        JPAExpressions.select(comment.count())
            .from(comment)
            .where(comment.review.eq(review)
                .and(comment.createdAt.between(start, end)))
    );

    // 이제 정상적으로 multiply, add 등의 숫자 연산이 가능합니다!
    NumberExpression<Double> score = periodLikes.doubleValue().multiply(0.3)
        .add(periodComments.doubleValue().multiply(0.7));

    return queryFactory
        .select(Projections.constructor(PopularReviewScoreDto.class,
            new Class<?>[]{UUID.class, Long.class, Long.class}, // 💡 이 줄이 추가되었습니다! (타입 명시)
            review.id, periodLikes, periodComments))
        .from(review)
        .where(review.deletedAt.isNull()
            .and(periodLikes.gt(0L).or(periodComments.gt(0L))))
        .orderBy(score.desc())
        .limit(limitSize)
        .fetch();
  }

  /**
   * 2. 파워 유저 통계 계산 쿼리
   */
  public List<PowerUserScoreDto> findTopPowerUsers(LocalDateTime start, LocalDateTime end, int limitSize) {
    QUsers user = QUsers.users;
    QReview review = QReview.review;
    QReviewLike reviewLike = QReviewLike.reviewLike;
    QComment comment = QComment.comment;

    // 1) 작성한 리뷰 인기 점수 합산
    // 해결 포인트: doubleValue()로 먼저 실수로 바꾸고, sum() 뒤에 coalesce(0.0)을 붙여서 DB에서 아예 0.0으로 가져옵니다!
    NumberExpression<Double> reviewScoreSum = Expressions.<Double>asNumber(
        JPAExpressions.select(
                review.likeCount.doubleValue().multiply(0.3)
                    .add(review.commentCount.doubleValue().multiply(0.7))
                    .sum().coalesce(0.0)
            )
            .from(review)
            .where(review.userId.eq(user.id)
                .and(review.createdAt.between(start, end)))
    );

    // 2) 내가 직접 누른 좋아요 수 (count는 기본적으로 0을 반환하므로 coalesce 불필요)
    NumberExpression<Long> periodLikes = Expressions.<Long>asNumber(
        JPAExpressions.select(reviewLike.count())
            .from(reviewLike)
            .where(reviewLike.user.id.eq(user.id)
                .and(reviewLike.createdAt.between(start, end)))
    );

    // 3) 내가 직접 쓴 댓글 수
    NumberExpression<Long> periodComments = Expressions.<Long>asNumber(
        JPAExpressions.select(comment.count())
            .from(comment)
            .where(comment.user.id.eq(user.id)
                .and(comment.createdAt.between(start, end)))
    );

    // 4) 최종 파워 유저 점수 계산
    NumberExpression<Double> totalScore = reviewScoreSum.multiply(0.5)
        .add(periodLikes.doubleValue().multiply(0.2))
        .add(periodComments.doubleValue().multiply(0.3));

    return queryFactory
        .select(Projections.constructor(PowerUserScoreDto.class,
            new Class<?>[]{UUID.class, Double.class, Long.class, Long.class}, // 💡 파워유저 타입 명시!
            user.id, reviewScoreSum, periodLikes, periodComments))
        .from(user)
        .where(user.deletedAt.isNull()
            .and(reviewScoreSum.gt(0.0)
                .or(periodLikes.gt(0L))
                .or(periodComments.gt(0L))))
        .orderBy(totalScore.desc())
        .limit(limitSize)
        .fetch();
  }
}