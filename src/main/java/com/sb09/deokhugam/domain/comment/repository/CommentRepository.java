package com.sb09.deokhugam.domain.comment.repository;

import com.sb09.deokhugam.domain.comment.entity.Comment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

  // deleted_at이 null인 댓글만 조회 (논리 삭제된 댓글 제외)
  List<Comment> findAllByReviewIdAndDeletedAtIsNull(UUID reviewId);
}
