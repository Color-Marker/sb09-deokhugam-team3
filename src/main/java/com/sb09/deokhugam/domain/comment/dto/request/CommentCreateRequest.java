package com.sb09.deokhugam.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CommentCreateRequest(
    @NotNull(message = "리뷰 ID는 필수입니다")
    UUID reviewId,
    @NotNull(message = "작성자 ID는 필수입니다")
    UUID userId,
    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 500, message = "메시지 내용은 500자 이하여야 합니다")
    String content
) {

}
