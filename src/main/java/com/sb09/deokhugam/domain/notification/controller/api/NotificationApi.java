package com.sb09.deokhugam.domain.notification.controller.api;

import com.sb09.deokhugam.domain.notification.dto.request.NotificationListRequest;
import com.sb09.deokhugam.domain.notification.dto.request.NotificationUpdateRequest;
import com.sb09.deokhugam.domain.notification.dto.response.NotificationDto;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Notification", description = "알림 관련 API")
public interface NotificationApi {

  @Operation(summary = "모든 알림 읽음 표시", description = "사용자의 모든 알림을 읽음 처리합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "알림 상태 업데이트 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "403", description = "알림 수정 권한 없음"),
      @ApiResponse(responseCode = "404", description = "알림 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<Void> readAll(
      @RequestHeader("Deokhugam-Request-User-ID")  @NotNull(message = "유저 ID는 필수입니다") UUID userId
  );

  @Operation(summary = "특정 알림 상태 변경", description = "특정 알림을 읽음 처리하거나 상태를 변경합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "알림 읽음 처리 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<NotificationDto> updateStatus(
      @PathVariable @NotNull(message = "알림 ID는 필수입니다") UUID notificationId,
      @RequestHeader("Deokhugam-Request-User-ID") @NotNull(message = "유저 ID는 필수입니다") UUID userId,
      @Valid @RequestBody NotificationUpdateRequest request
  );

  @Operation(summary = "알림 목록 조회", description = "커서 기반 페이징으로 알림 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<CursorPageResponseDto<NotificationDto>> list(
      @Valid NotificationListRequest request
  );
}
