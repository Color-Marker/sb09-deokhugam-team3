package com.sb09.deokhugam.domain.comment.repository;

import com.sb09.deokhugam.domain.comment.entity.Comment;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

  @Query("SELECT c FROM Comment c "
      + "WHERE c.review.id = :reviewId AND c.deletedAt IS NULL "
      + "AND ((:after IS NULL OR c.createdAt < :after) "
      + "OR (c.createdAt = :after AND c.id < :cursor)) "
      + "ORDER BY c.createdAt DESC")
  Slice<Comment> findCommentsDesc(
      @Param("reviewId") UUID reviewId,
      @Param("after") LocalDateTime after,
      @Param("cursor") UUID cursor,
      Pageable pageable);

  @Query("SELECT c FROM Comment "
      + "c WHERE c.review.id = :reviewId AND c.deletedAt IS NULL "
      + "AND ((:after IS NULL OR c.createdAt > :after) "
      + "OR (c.createdAt = :after AND c.id > :cursor)) "
      + "ORDER BY c.createdAt ASC")
  Slice<Comment> findCommentsAsc(
      @Param("reviewId") UUID reviewId,
      @Param("after") LocalDateTime after,
      @Param("cursor") UUID cursor,
      Pageable pageable);

  Long countByReviewIdAndDeletedAtIsNull(UUID reviewId);
}
