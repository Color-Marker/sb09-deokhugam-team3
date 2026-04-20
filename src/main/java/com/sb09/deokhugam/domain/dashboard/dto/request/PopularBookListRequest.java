package com.sb09.deokhugam.domain.dashboard.dto.request;

import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

@NoArgsConstructor
@Data
public class PopularBookListRequest {
  private PeriodType period = PeriodType.DAILY;
  private Sort.Direction direction = Sort.Direction.ASC;
  private Object cursor;
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
  private LocalDateTime after;
  @NotNull(message = "제한 범위는 필수입니다")
  @Min(value = 1, message = "최소 1개의 데이터 제한은 필수입니다.")
  private int limit = 50;
}
