package com.sb09.deokhugam.global.Exception.comment;

import com.sb09.deokhugam.global.Exception.ErrorCode;

public class CommentAlreadyDeletedException extends CommentException {

  public CommentAlreadyDeletedException() {
    super(ErrorCode.DELETED_COMMENT);
  }

  public static CommentAlreadyDeletedException withId(Long commentId) {
    CommentAlreadyDeletedException exception = new CommentAlreadyDeletedException();
    exception.addDetail("commentId", commentId);
    return exception;
  }


}
