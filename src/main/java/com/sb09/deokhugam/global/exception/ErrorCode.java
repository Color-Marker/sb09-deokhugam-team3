package com.sb09.deokhugam.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  // User
  USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  DUPLICATE_EMAIL("이미 존재하는 이메일입니다.", HttpStatus.CONFLICT),
  INVALID_USER_CREDENTIALS("이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
  UNAUTHORIZED_ACCESS("접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
  DELETED_USER("탈퇴한 사용자입니다.", HttpStatus.GONE),

  // Book
  BOOK_NOT_FOUND("도서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  DUPLICATE_ISBN("이미 등록된 ISBN입니다.", HttpStatus.CONFLICT),
  INVALID_ISBN_FORMAT("올바르지 않은 ISBN 형식입니다.", HttpStatus.BAD_REQUEST),

  // Review
  REVIEW_NOT_FOUND("리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  DUPLICATE_REVIEW("해당 도서에 이미 리뷰를 작성했습니다.", HttpStatus.CONFLICT),
  // schema에 UNIQUE (book_id, user_id) 제약 반영
  DELETED_REVIEW("삭제된 리뷰입니다.", HttpStatus.GONE),

  // Comment
  COMMENT_NOT_FOUND("댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  DELETED_COMMENT("이미 삭제된 댓글입니다.", HttpStatus.GONE),
  COMMENT_UPDATE_FORBIDDEN("댓글을 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN),
  COMMENT_DELETE_FORBIDDEN("댓글을 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN),

  // Review Like
  DUPLICATE_REVIEW_LIKE("이미 좋아요를 누른 리뷰입니다.", HttpStatus.CONFLICT),
  // schema에 UNIQUE (review_id, user_id) 제약 반영
  REVIEW_LIKE_NOT_FOUND("좋아요 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

  // Notification
  NOTIFICATION_NOT_FOUND("알림을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  NOTIFICATION_ACCESS_FORBIDDEN("알림에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),

  // Common / Server
  VALIDATION_ERROR("요청 데이터가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
  INVALID_REQUEST("잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
  INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String message;
  private final HttpStatus status;

  ErrorCode(String message, HttpStatus status) {
    this.message = message;
    this.status = status;
  }


}
