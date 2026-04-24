package com.sb09.deokhugam.domain.notification.mapper;

import com.sb09.deokhugam.domain.notification.dto.response.NotificationDto;
import com.sb09.deokhugam.domain.notification.entity.Notification;
import com.sb09.deokhugam.domain.notification.entity.NotificationType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "reviewId", source = "review.id")
  @Mapping(target = "reviewContent", source = "review.content")
  @Mapping(target = "confirmed", source = "confirmStatus")
  @Mapping(target = "message", expression = "java(createMessage(notification))")
  NotificationDto toDto(Notification notification);

  default String createMessage(Notification notification) {
    if (notification.getType().equals(NotificationType.RANKING)) {
      return "당신의 리뷰가 인기 순위 10위 내에 선정되었습니다.";
    }
    String nickname = notification.getSender() != null
        ? notification.getSender().getNickname() : "알 수 없음";
    if (notification.getType().equals(NotificationType.LIKE)) {
      return "[" + nickname + "]님이 나의 리뷰를 좋아합니다.";
    } else {
      return "[" + nickname + "]님이 나의 리뷰에 댓글을 남겼습니다.";
    }
  }
}
