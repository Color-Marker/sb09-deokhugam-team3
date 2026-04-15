package com.sb09.deokhugam.global.Exception.comment;

import com.sb09.deokhugam.global.Exception.ErrorCode;
import java.util.UUID;

public class CommentNotFoundException extends CommentException {

  public CommentNotFoundException() {
    super(ErrorCode.COMMENT_NOT_FOUND);
  }

  public static CommentNotFoundException withId(UUID commentId) {
    CommentNotFoundException exception = new CommentNotFoundException();
    exception.addDetail("commentId", commentId);
    return exception;
  }
}
