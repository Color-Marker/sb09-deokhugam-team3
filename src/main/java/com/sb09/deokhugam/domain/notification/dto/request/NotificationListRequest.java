package com.sb09.deokhugam.domain.notification.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

@NoArgsConstructor
@Data
public class NotificationListRequest {
  @NotNull(message = "유저 ID는 필수입니다")
  private UUID userId;
  private Object cursor;
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
  private LocalDateTime after;
  @NotNull(message = "제한 범위는 필수입니다")
  @Min(value = 1, message = "최소 1개의 데이터 제한은 필수입니다.")
  private int limit = 20;
  private Sort.Direction direction = Sort.Direction.DESC;
}
