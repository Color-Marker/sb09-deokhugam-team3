package com.sb09.deokhugam.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

@NoArgsConstructor
@Getter
public class CommentListRequest {

  @NotNull(message = "리뷰 ID는 필수입니다")
  private UUID reviewId;

  private UUID cursor;

  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
  private LocalDateTime after;

  @Schema(description = "페이지 크기", defaultValue = "50", minimum = "1", maximum = "100")
  @Min(value = 1, message = "최소 1개 이상이어야 합니다.")
  @Max(value = 100, message = "최대 100개까지 조회 가능합니다.")
  private int limit = 50;

  @Schema(description = "정렬 방향", defaultValue = "DESC", allowableValues = {"ASC", "DESC"})
  private Sort.Direction direction = Sort.Direction.DESC;
}