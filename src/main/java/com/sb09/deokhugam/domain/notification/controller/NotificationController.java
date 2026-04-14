package com.sb09.deokhugam.domain.notification.controller;

import com.sb09.deokhugam.domain.notification.dto.request.NotificationListRequest;
import com.sb09.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.sb09.deokhugam.domain.notification.dto.response.NotificationDto;
import com.sb09.deokhugam.domain.notification.service.NotificationService;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @PatchMapping("/read-all")
  public ResponseEntity<Void> readAll(
      @RequestHeader("Deokhugam-Request-User-ID") UUID userId
  ){
    return notificationService.readAll(userId);
  }

  @PatchMapping("/{notificationId}")
  public ResponseEntity<NotificationDto> updateStatus(
      @PathVariable UUID notificationId,
      @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
      @RequestBody NotificationUpdateRequest request
  ){
    NotificationDto result = notificationService.updateStatus(notificationId, userId, request);
    return ResponseEntity.ok(result);
  }

  @GetMapping
  public ResponseEntity<CursorPageResponseDto<NotificationDto>> list(NotificationListRequest request){
    CursorPageResponseDto<NotificationDto> result = notificationService.list(request);
    return ResponseEntity.ok(result);
  }

}
