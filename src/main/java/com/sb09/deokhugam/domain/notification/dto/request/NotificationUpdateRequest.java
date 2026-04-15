package com.sb09.deokhugam.domain.notification.dto.request;

import software.amazon.awssdk.annotations.NotNull;

public record NotificationUpdateRequest(
    @NotNull
    boolean confirmed
) {

}
