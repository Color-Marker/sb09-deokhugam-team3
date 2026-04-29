package com.sb09.deokhugam.domain.user.controller.api;

import com.sb09.deokhugam.domain.book.dto.PopularBookDto;
import com.sb09.deokhugam.domain.dashboard.entity.PeriodType;
import com.sb09.deokhugam.domain.dashboard.entity.PowerUser;
import com.sb09.deokhugam.domain.user.dto.Response.PowerUserDto;
import com.sb09.deokhugam.domain.user.dto.Response.UserResponse;
import com.sb09.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.sb09.deokhugam.domain.user.dto.request.UserRegisterRequest;
import com.sb09.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.sb09.deokhugam.global.common.dto.CursorPageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "사용자 관리", description = "사용자 관련 API")
public interface UserApi {

  @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "회원가입 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "409", description = "이메일 중복")
  })
  ResponseEntity<UserResponse> register(@RequestBody UserRegisterRequest request);

  @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그인 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "401", description = "로그인 실패 (이메일 또는 비밀번호 불일치)")
  })
  ResponseEntity<UserResponse> login(@RequestBody UserLoginRequest request);

  @Operation(summary = "사용자 정보 조회", description = "사용자 ID로 상세 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음")
  })
  ResponseEntity<UserResponse> getUser(
      @Parameter(description = "사용자 ID") @PathVariable UUID userId
  );

  @Operation(summary = "사용자 정보 수정", description = "사용자의 닉네임을 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음")
  })
  ResponseEntity<UserResponse> updateUser(
      @Parameter(description = "사용자 ID") @PathVariable UUID userId,
      @Parameter(description = "요청자 ID") @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
      @RequestBody UserUpdateRequest request
  );

  @Operation(summary = "사용자 논리 삭제", description = "사용자를 논리적으로 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음")
  })
  ResponseEntity<Void> deleteUser(
      @Parameter(description = "사용자 ID") @PathVariable UUID userId,
      @Parameter(description = "요청자 ID") @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
  );

  @Operation(summary = "사용자 물리 삭제", description = "사용자를 물리적으로 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음")
  })
  ResponseEntity<Void> hardDeleteUser(
      @Parameter(description = "사용자 ID") @PathVariable UUID userId
  );

  @Operation(summary = "파워 유저 목록 조회", description = "기간별 파워 유저 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "파워 유저 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<CursorPageResponseDto<PowerUserDto>> getPowerUsers(
      @Parameter(description = "기간 (DAILY/WEEKLY/MONTHLY/ALL_TIME)") @RequestParam PeriodType period,
      @Parameter(description = "커서 값") @RequestParam(required = false) Long cursor,
      @Parameter(description = "커서 시간") @RequestParam(required = false) LocalDateTime after,
      @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int limit
  );
}
