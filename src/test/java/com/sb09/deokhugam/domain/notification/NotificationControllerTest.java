package com.sb09.deokhugam.domain.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb09.deokhugam.config.RequestTrackingFilter;
import com.sb09.deokhugam.domain.notification.controller.NotificationController;
import com.sb09.deokhugam.domain.notification.dto.request.NotificationListRequest;
import com.sb09.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.sb09.deokhugam.domain.notification.dto.response.NotificationDto;
import com.sb09.deokhugam.domain.notification.service.NotificationService;
import com.sb09.deokhugam.global.Exception.CustomException;
import com.sb09.deokhugam.global.Exception.ErrorCode;
import com.sb09.deokhugam.global.Exception.notification.NotificationForbiddenException;
import com.sb09.deokhugam.global.Exception.notification.NotificationNotFoundException;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = NotificationController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = RequestTrackingFilter.class
    )
)
public class NotificationControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockitoBean
  private NotificationService notificationService;

  private final String userIdHeader = "Deokhugam-Request-User-ID";

  @Test
  @DisplayName("모든 알림 읽음 처리 - 성공")
  void readAll_success() throws Exception {
    UUID userId = UUID.randomUUID();

    mockMvc.perform(patch("/api/notifications/read-all")
            .header(userIdHeader, userId.toString()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("모든 알림 읽음 처리 - 잘못된 요청 예외 (사용자 ID 누락)")
  void readAll_fail_missingUserId() throws Exception {
    mockMvc.perform(patch("/api/notifications/read-all"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("모든 알림 읽음 처리 - 사용자 조회 실패 예외")
  void readAll_fail_userNotFound() throws Exception {
    UUID userId = UUID.randomUUID();
    doThrow(new CustomException(ErrorCode.USER_NOT_FOUND))
        .when(notificationService).readAll(userId);
    mockMvc.perform(patch("/api/notifications/read-all")
            .header(userIdHeader, userId.toString()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("특정 알림 상태 업데이트 - 성공")
  void updateStatus_success() throws Exception {
    UUID notificationId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    NotificationUpdateRequest request = new NotificationUpdateRequest(true);
    NotificationDto response = new NotificationDto(
        notificationId, userId, UUID.randomUUID(), "리뷰 내용", "메시지", true, LocalDateTime.now(), LocalDateTime.now()
    );

    given(notificationService.updateStatus(eq(notificationId), eq(userId), any(NotificationUpdateRequest.class)))
        .willReturn(response);

    mockMvc.perform(patch("/api/notifications/{notificationId}", notificationId)
            .header(userIdHeader, userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(notificationId.toString()))
        .andExpect(jsonPath("$.confirmed").value(true));
  }

  @Test
  @DisplayName("특정 알림 상태 업데이트 - 잘못된 요청 예외")
  void updateStatus_fail_badRequest() throws Exception {
    UUID notificationId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String badRequest = "{\"confirmed\": false }";
    given(notificationService.updateStatus(eq(notificationId), eq(userId), any(NotificationUpdateRequest.class)))
        .willThrow(new CustomException(ErrorCode.INVALID_REQUEST));

    mockMvc.perform(patch("/api/notifications/{notificationId}", notificationId)
        .header(userIdHeader, userId.toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(badRequest))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("특정 알림 상태 업데이트 - 알림 수정 권한 예외")
  void updateStatus_fail_permissionDenied() throws Exception {
    UUID notificationId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    NotificationUpdateRequest request = new NotificationUpdateRequest(true);
    given(notificationService.updateStatus(eq(notificationId), eq(userId), any(NotificationUpdateRequest.class)))
        .willThrow(new NotificationForbiddenException(ErrorCode.NOTIFICATION_ACCESS_FORBIDDEN));
    mockMvc.perform(patch("/api/notifications/{notificationId}", notificationId)
            .header(userIdHeader, userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("특정 알림 상태 업데이트 - 알림 조회 실패 예외")
  void updateStatus_fail_notificationNotFound() throws Exception {
    UUID notificationId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    NotificationUpdateRequest request = new NotificationUpdateRequest(true);
    given(notificationService.updateStatus(eq(notificationId), eq(userId), any(NotificationUpdateRequest.class)))
        .willThrow(NotificationNotFoundException.withId(notificationId));
    mockMvc.perform(patch("/api/notifications/{notificationId}", notificationId)
            .header(userIdHeader, userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("알림 목록 조회 - 성공")
  void list_success() throws Exception {
    UUID userId = UUID.randomUUID();
    CursorPageResponseDto<NotificationDto> response = new CursorPageResponseDto<>(
        List.of(), null, LocalDateTime.now(), 10, null, false
    );

    given(notificationService.list(any(NotificationListRequest.class)))
        .willReturn(response);

    mockMvc.perform(get("/api/notifications")
            .param("userId", userId.randomUUID().toString())
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  @DisplayName("알림 목록 조회 - 잘못된 요청입니다. (사용자 ID 누락)")
  void list_fail_badRequest() throws Exception {
    given(notificationService.list(any(NotificationListRequest.class)))
        .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));
    mockMvc.perform(get("/api/notifications"))
        .andExpect(status().isBadRequest());

  }
  @Test
  @DisplayName("알림 목록 조회 - 사용자 조회 실패 예외")
  void list_fail_userNotFound() throws Exception {
    UUID userId = UUID.randomUUID();
    given(notificationService.list(any(NotificationListRequest.class)))
        .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));
    mockMvc.perform(get("/api/notifications")
            .param("userId", userId.toString()) // userId를 인자로 넣어줘야 400을 피함
            .param("size", "10"))
        .andExpect(status().isNotFound());
  }

}
