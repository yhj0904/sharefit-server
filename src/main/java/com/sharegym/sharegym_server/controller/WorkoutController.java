package com.sharegym.sharegym_server.controller;

import com.sharegym.sharegym_server.dto.request.AddExerciseRequest;
import com.sharegym.sharegym_server.dto.request.CheerRequest;
import com.sharegym.sharegym_server.dto.request.CreateWorkoutRequest;
import com.sharegym.sharegym_server.dto.request.SetRequest;
import com.sharegym.sharegym_server.dto.response.ApiResponse;
import com.sharegym.sharegym_server.dto.response.WorkoutResponse;
import com.sharegym.sharegym_server.dto.response.WorkoutSessionResponse;
import com.sharegym.sharegym_server.security.CurrentUser;
import com.sharegym.sharegym_server.security.UserPrincipal;
import com.sharegym.sharegym_server.service.WorkoutService;
import com.sharegym.sharegym_server.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 운동 관련 컨트롤러
 * 프론트엔드 API 규격을 지원하는 엔드포인트 포함
 */
@Slf4j
@RestController
@RequestMapping("/workouts")
@RequiredArgsConstructor
@Tag(name = "Workout", description = "운동 관련 API")
public class WorkoutController {

    private final WorkoutService workoutService;
    private final NotificationService notificationService;

    /**
     * 운동 세션 저장 (프론트엔드 규격)
     * 프론트엔드 API 규격: POST /api/v1/workouts
     * 운동 완료 후 전체 세션 데이터를 저장
     */
    @PostMapping
    @Operation(summary = "운동 세션 저장", description = "완료된 운동 세션을 저장합니다.")
    public ResponseEntity<WorkoutSessionResponse> saveWorkoutSession(
        @CurrentUser UserPrincipal userPrincipal,
        @RequestBody Map<String, Object> sessionData) {
        log.info("Save workout session for user: {}", userPrincipal.getId());
        WorkoutSessionResponse response = workoutService.saveWorkoutSession(userPrincipal.getId(), sessionData);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 사용자 운동 히스토리 조회 (프론트엔드 규격)
     * 프론트엔드 API 규격: GET /api/v1/workouts/users/{userId}
     */
    @GetMapping("/users/{userId}")
    @Operation(summary = "사용자 운동 히스토리", description = "특정 사용자의 운동 히스토리를 조회합니다.")
    public ResponseEntity<List<WorkoutSessionResponse>> getUserWorkoutHistory(
        @PathVariable Long userId) {
        log.info("Get workout history for user: {}", userId);
        List<WorkoutSessionResponse> response = workoutService.getUserWorkoutHistory(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 마지막 운동 조회 (프론트엔드 규격)
     * 프론트엔드 API 규격: GET /api/v1/workouts/users/{userId}/last
     */
    @GetMapping("/users/{userId}/last")
    @Operation(summary = "마지막 운동 조회", description = "사용자의 가장 최근 운동을 조회합니다.")
    public ResponseEntity<WorkoutSessionResponse> getLastWorkout(
        @PathVariable Long userId) {
        log.info("Get last workout for user: {}", userId);
        WorkoutSessionResponse response = workoutService.getLastWorkout(userId);
        return ResponseEntity.ok(response);
    }

    // ============= 기존 백엔드 API (유지) =============

    /**
     * 운동 세션 생성 (백엔드 기존 방식)
     */
    @PostMapping("/start")
    @Operation(summary = "운동 시작", description = "새로운 운동 세션을 시작합니다.")
    public ResponseEntity<ApiResponse<WorkoutResponse>> createWorkout(
        @CurrentUser UserPrincipal userPrincipal,
        @Valid @RequestBody CreateWorkoutRequest request) {
        log.info("Create workout for user: {}", userPrincipal.getId());
        WorkoutResponse response = workoutService.createWorkout(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response));
    }

    /**
     * 운동 조회
     */
    @GetMapping("/{workoutId}")
    @Operation(summary = "운동 상세 조회", description = "특정 운동 세션의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<WorkoutResponse>> getWorkout(
        @PathVariable Long workoutId,
        @CurrentUser UserPrincipal userPrincipal) {
        log.info("Get workout: {} for user: {}", workoutId, userPrincipal.getId());
        WorkoutResponse response = workoutService.getWorkout(workoutId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 사용자 운동 목록 조회
     */
    @GetMapping("/list")
    @Operation(summary = "운동 목록 조회", description = "로그인한 사용자의 운동 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<WorkoutResponse>>> getUserWorkouts(
        @CurrentUser UserPrincipal userPrincipal,
        @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Get user workouts for user: {}", userPrincipal.getId());
        Page<WorkoutResponse> response = workoutService.getUserWorkouts(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 기간별 운동 조회
     */
    @GetMapping("/history")
    @Operation(summary = "기간별 운동 조회", description = "특정 기간의 운동 기록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<WorkoutResponse>>> getWorkoutHistory(
        @CurrentUser UserPrincipal userPrincipal,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Get workout history for user: {} from {} to {}",
            userPrincipal.getId(), startDate, endDate);
        List<WorkoutResponse> response = workoutService.getUserWorkoutsByPeriod(
            userPrincipal.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 운동 추가
     */
    @PostMapping("/{workoutId}/exercises")
    @Operation(summary = "운동 추가", description = "운동 세션에 새로운 운동을 추가합니다.")
    public ResponseEntity<ApiResponse<WorkoutResponse>> addExercise(
        @PathVariable Long workoutId,
        @CurrentUser UserPrincipal userPrincipal,
        @Valid @RequestBody AddExerciseRequest request) {
        log.info("Add exercise to workout: {} for user: {}", workoutId, userPrincipal.getId());
        WorkoutResponse response = workoutService.addExercise(userPrincipal.getId(), workoutId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 세트 추가/업데이트
     */
    @PutMapping("/{workoutId}/exercises/{exerciseId}/sets")
    @Operation(summary = "세트 추가/업데이트", description = "운동의 세트 정보를 추가하거나 업데이트합니다.")
    public ResponseEntity<ApiResponse<WorkoutResponse>> addOrUpdateSet(
        @PathVariable Long workoutId,
        @PathVariable Long exerciseId,
        @CurrentUser UserPrincipal userPrincipal,
        @Valid @RequestBody SetRequest request) {
        log.info("Update set for workout: {} exercise: {} user: {}",
            workoutId, exerciseId, userPrincipal.getId());
        WorkoutResponse response = workoutService.addOrUpdateSet(
            userPrincipal.getId(), workoutId, exerciseId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 운동 완료
     */
    @PutMapping("/{workoutId}/complete")
    @Operation(summary = "운동 완료", description = "운동 세션을 완료 처리합니다.")
    public ResponseEntity<ApiResponse<WorkoutResponse>> completeWorkout(
        @PathVariable Long workoutId,
        @CurrentUser UserPrincipal userPrincipal) {
        log.info("Complete workout: {} for user: {}", workoutId, userPrincipal.getId());
        WorkoutResponse response = workoutService.completeWorkout(userPrincipal.getId(), workoutId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 운동 취소
     */
    @PutMapping("/{workoutId}/cancel")
    @Operation(summary = "운동 취소", description = "진행 중인 운동 세션을 취소합니다.")
    public ResponseEntity<ApiResponse<Void>> cancelWorkout(
        @PathVariable Long workoutId,
        @CurrentUser UserPrincipal userPrincipal) {
        log.info("Cancel workout: {} for user: {}", workoutId, userPrincipal.getId());
        workoutService.cancelWorkout(userPrincipal.getId(), workoutId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 운동 삭제
     */
    @DeleteMapping("/{workoutId}")
    @Operation(summary = "운동 삭제", description = "운동 세션을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteWorkout(
        @PathVariable Long workoutId,
        @CurrentUser UserPrincipal userPrincipal) {
        log.info("Delete workout: {} for user: {}", workoutId, userPrincipal.getId());
        workoutService.deleteWorkout(userPrincipal.getId(), workoutId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 운동 중인 사용자에게 응원 전송
     * Frontend: POST /api/v1/workouts/{workoutId}/cheers
     */
    @PostMapping("/{workoutId}/cheers")
    @Operation(summary = "운동 응원", description = "운동 중인 사용자에게 응원 메시지를 전송합니다")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendCheer(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long workoutId,
            @Valid @RequestBody CheerRequest request) {

        log.info("User {} sending cheer to workout {}: {}",
                 userPrincipal.getId(), workoutId, request.getMessage());

        // 운동 정보 조회
        WorkoutResponse workout = workoutService.getWorkout(workoutId, userPrincipal.getId());

        // 본인 운동에는 응원 불가
        if (workout.getUserId().equals(userPrincipal.getId())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("본인 운동에는 응원할 수 없습니다."));
        }

        // SSE로 응원 알림 전송
        notificationService.notifyCheer(
            workout.getUserId(),
            userPrincipal.getId(),
            userPrincipal.getUsername(),
            request.getMessage()
        );

        Map<String, Object> result = Map.of(
            "success", true,
            "message", "응원이 전송되었습니다."
        );

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}