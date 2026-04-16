package com.sb09.deokhugam.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 500, message = "메시지 내용은 500자 이하여야 합니다")
    String content
) {

}
