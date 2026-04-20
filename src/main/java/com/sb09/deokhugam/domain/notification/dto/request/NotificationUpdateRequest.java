package com.sb09.deokhugam.domain.notification.dto.request;


import jakarta.validation.constraints.NotNull;

public record NotificationUpdateRequest(
    @NotNull(message = "확인 여부는 필수입니다")
    Boolean confirmed
) {

}
