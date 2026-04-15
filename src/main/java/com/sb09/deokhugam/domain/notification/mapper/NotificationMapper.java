package com.sb09.deokhugam.domain.notification.mapper;

import com.sb09.deokhugam.domain.notification.dto.response.NotificationDto;
import com.sb09.deokhugam.domain.notification.entity.Notification;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
  @Mapping(target = "id", source = "id")
  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "reviewId", source = "review.id")
  @Mapping(target = "reviewContent", source = "review.content")
  @Mapping(target = "confirmed", source = "confirmStatus")
  @Mapping(target = "message", source = "message")
  NotificationDto toDto(Notification notification, String message);
}
