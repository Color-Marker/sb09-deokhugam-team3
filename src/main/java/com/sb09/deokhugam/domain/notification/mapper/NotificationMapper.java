package com.sb09.deokhugam.domain.notification.mapper;

import com.sb09.deokhugam.domain.notification.dto.response.NotificationDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public class NotificationMapper {
  NotificationDto toDto();
}
