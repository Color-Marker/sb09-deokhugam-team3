package com.sb09.deokhugam.domain.user.dto.Response;

import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PowerUserDto(
    UUID userId,
    String nickname,
    PeriodType period,
    LocalDateTime createdAt,
    Long rank,
    BigDecimal score,
    BigDecimal reviewScoreSum,
    Long likeCount,
    Long commentCount
) {
}
