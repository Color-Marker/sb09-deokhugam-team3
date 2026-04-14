package com.sb09.deokhugam.domain.notification.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
public class NotificationListRequest {
  private UUID userId;
  private Object cursor;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime after;
  private int limit = 20;
  private Sort.Direction direction = Sort.Direction.DESC;
}
