package com.sharegym.sharegym_server.service;

import com.sharegym.sharegym_server.common.ExerciseIdMapper;
import com.sharegym.sharegym_server.dto.request.AddExerciseRequest;
import com.sharegym.sharegym_server.dto.request.CreateWorkoutRequest;
import com.sharegym.sharegym_server.dto.request.SetRequest;
import com.sharegym.sharegym_server.dto.response.WorkoutResponse;
import com.sharegym.sharegym_server.dto.response.WorkoutSessionResponse;
import com.sharegym.sharegym_server.entity.*;
import com.sharegym.sharegym_server.exception.BusinessException;
import com.sharegym.sharegym_server.exception.ErrorCode;
import com.sharegym.sharegym_server.repository.ExerciseRepository;
import com.sharegym.sharegym_server.repository.UserRepository;
import com.sharegym.sharegym_server.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 운동 관련 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseIdMapper exerciseIdMapper;
    private final NotificationService notificationService;

    /**
     * 운동 세션 생성
     */
    @Transactional
    public WorkoutResponse createWorkout(Long userId, CreateWorkoutRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 진행 중인 운동이 있는지 확인
        workoutRepository.findInProgressWorkout(user)
            .ifPresent(w -> {
                throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "이미 진행 중인 운동이 있습니다. 먼저 완료하거나 취소해주세요.");
            });

        // 운동 세션 생성
        Workout workout = Workout.builder()
            .user(user)
            .workoutName(request.getWorkoutName())
            .startTime(request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now())
            .note(request.getNote())
            .status(Workout.WorkoutStatus.IN_PROGRESS)
            .build();

        // 루틴 기반 시작인 경우 운동 추가
        if (request.getRoutineId() != null) {
            // TODO: 루틴 서비스 구현 후 처리
        }

        Workout savedWorkout = workoutRepository.save(workout);
        log.info("Workout session created: {} for user: {}", savedWorkout.getId(), user.getEmail());

        // SSE 알림 전송
        notificationService.notifyWorkoutStart(savedWorkout);

        return WorkoutResponse.from(savedWorkout);
    }

    /**
     * 운동 추가
     */
    @Transactional
    public WorkoutResponse addExercise(Long userId, Long workoutId, AddExerciseRequest request) {
        Workout workout = getWorkoutWithPermission(workoutId, userId);

        if (workout.getStatus() != Workout.WorkoutStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.WORKOUT_ALREADY_COMPLETED);
        }

        // 클라이언트 ID를 서버 ID로 변환
        Integer exerciseServerId = exerciseIdMapper.toServerId(request.getExerciseId());
        Exercise exercise = exerciseRepository.findById(exerciseServerId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EXERCISE_NOT_FOUND));

        // 운동 추가
        WorkoutExercise workoutExercise = WorkoutExercise.builder()
            .workout(workout)
            .exercise(exercise)
            .orderIndex(request.getOrderIndex())
            .targetSets(request.getTargetSets())
            .targetReps(request.getTargetReps())
            .targetWeight(request.getTargetWeight())
            .note(request.getNote())
            .build();

        workout.addExercise(workoutExercise);
        Workout savedWorkout = workoutRepository.save(workout);

        log.info("Exercise added to workout: {} - {}", workoutId, exercise.getName());
        return WorkoutResponse.from(savedWorkout);
    }

    /**
     * 세트 추가/업데이트
     */
    @Transactional
    public WorkoutResponse addOrUpdateSet(Long userId, Long workoutId,
                                         Long exerciseId, SetRequest request) {
        Workout workout = getWorkoutWithPermission(workoutId, userId);

        if (workout.getStatus() != Workout.WorkoutStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.WORKOUT_ALREADY_COMPLETED);
        }

        // 운동 찾기
        WorkoutExercise workoutExercise = workout.getExercises().stream()
            .filter(we -> we.getId().equals(exerciseId))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.EXERCISE_NOT_FOUND,
                "해당 운동을 찾을 수 없습니다."));

        // 기존 세트 찾기 또는 새로 생성
        WorkoutSet workoutSet = workoutExercise.getSets().stream()
            .filter(s -> s.getSetNumber().equals(request.getSetNumber()))
            .findFirst()
            .orElseGet(() -> {
                WorkoutSet newSet = WorkoutSet.builder()
                    .workoutExercise(workoutExercise)
                    .setNumber(request.getSetNumber())
                    .build();
                workoutExercise.addSet(newSet);
                return newSet;
            });

        // 세트 정보 업데이트
        workoutSet.setReps(request.getReps());
        workoutSet.setWeight(request.getWeight());
        workoutSet.setDistance(request.getDistance());
        workoutSet.setDurationSeconds(request.getDurationSeconds());
        workoutSet.setLevel(request.getLevel());
        workoutSet.setSetType(request.getSetType());
        workoutSet.setIsCompleted(request.getIsCompleted());
        workoutSet.setRestSeconds(request.getRestSeconds());
        workoutSet.setNote(request.getNote());

        Workout savedWorkout = workoutRepository.save(workout);

        log.info("Set updated for workout: {} exercise: {} set: {}",
            workoutId, exerciseId, request.getSetNumber());
        return WorkoutResponse.from(savedWorkout);
    }

    /**
     * 운동 완료
     */
    @Transactional
    public WorkoutResponse completeWorkout(Long userId, Long workoutId) {
        Workout workout = getWorkoutWithPermission(workoutId, userId);

        if (workout.getStatus() != Workout.WorkoutStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.WORKOUT_ALREADY_COMPLETED);
        }

        // 운동 완료 처리
        workout.complete();

        // 사용자 통계 업데이트
        User user = workout.getUser();
        user.incrementWorkoutCount();
        // TODO: 스트릭 계산 로직 추가

        workoutRepository.save(workout);
        userRepository.save(user);

        // SSE 알림 전송
        notificationService.notifyWorkoutComplete(workout);

        log.info("Workout completed: {} for user: {}", workoutId, user.getEmail());
        return WorkoutResponse.from(workout);
    }

    /**
     * 운동 취소
     */
    @Transactional
    public void cancelWorkout(Long userId, Long workoutId) {
        Workout workout = getWorkoutWithPermission(workoutId, userId);

        if (workout.getStatus() != Workout.WorkoutStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                "진행 중인 운동만 취소할 수 있습니다.");
        }

        workout.setStatus(Workout.WorkoutStatus.CANCELLED);
        workoutRepository.save(workout);

        log.info("Workout cancelled: {} for user: {}", workoutId, workout.getUser().getEmail());
    }

    /**
     * 운동 삭제
     */
    @Transactional
    public void deleteWorkout(Long userId, Long workoutId) {
        Workout workout = getWorkoutWithPermission(workoutId, userId);
        workoutRepository.delete(workout);

        log.info("Workout deleted: {} for user: {}", workoutId, workout.getUser().getEmail());
    }

    /**
     * 운동 조회
     */
    @Transactional(readOnly = true)
    public WorkoutResponse getWorkout(Long workoutId, Long userId) {
        Workout workout = workoutRepository.findById(workoutId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORKOUT_NOT_FOUND));

        // 본인 운동이 아닌 경우 공개 여부 확인 (추후 구현)
        if (!workout.getUser().getId().equals(userId)) {
            // TODO: 공개 운동인지 확인
        }

        return WorkoutResponse.from(workout);
    }

    /**
     * 사용자 운동 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<WorkoutResponse> getUserWorkouts(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Page<Workout> workouts = workoutRepository.findByUserOrderByStartTimeDesc(user, pageable);
        return workouts.map(WorkoutResponse::fromSimple);
    }

    /**
     * 기간별 운동 조회
     */
    @Transactional(readOnly = true)
    public List<WorkoutResponse> getUserWorkoutsByPeriod(Long userId,
                                                         LocalDateTime startTime,
                                                         LocalDateTime endTime) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Workout> workouts = workoutRepository.findByUserAndStartTimeBetweenOrderByStartTimeDesc(
            user, startTime, endTime);

        return workouts.stream()
            .map(WorkoutResponse::fromSimple)
            .collect(Collectors.toList());
    }

    /**
     * 운동 권한 확인 및 조회
     */
    private Workout getWorkoutWithPermission(Long workoutId, Long userId) {
        Workout workout = workoutRepository.findById(workoutId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORKOUT_NOT_FOUND));

        if (!workout.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED,
                "해당 운동에 대한 권한이 없습니다.");
        }

        return workout;
    }

    // ==================== 프론트엔드 API 규격 메서드 ====================

    /**
     * 운동 세션 저장 (프론트엔드 규격)
     * 프론트엔드에서 완료된 운동 세션 전체를 전송
     */
    @Transactional
    public WorkoutSessionResponse saveWorkoutSession(Long userId, Map<String, Object> sessionData) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // WorkoutSessionResponse로 변환 (클라이언트 데이터 구조 파싱)
        WorkoutSessionResponse sessionResponse = WorkoutSessionResponse.fromMap(sessionData);

        // 새로운 Workout 엔티티 생성
        Workout workout = Workout.builder()
            .user(user)
            .workoutName(sessionResponse.getTitle() != null ? sessionResponse.getTitle() : "운동")
            .startTime(sessionResponse.getStartTime())
            .endTime(sessionResponse.getEndTime())
            .totalVolume(sessionResponse.getTotalVolume())
            .totalSets(sessionResponse.getTotalSets())
            .duration(sessionResponse.getDuration() != null ?
                (int) (sessionResponse.getDuration() / 1000) : 0) // milliseconds to seconds
            .calories(sessionResponse.getCaloriesBurned())
            .note(sessionResponse.getNotes())
            .status(Workout.WorkoutStatus.COMPLETED)
            .build();

        // 운동별 데이터 저장
        if (sessionResponse.getExercises() != null) {
            for (WorkoutSessionResponse.WorkoutExerciseData exerciseData : sessionResponse.getExercises()) {
                // 운동 ID 변환 (문자열 -> 숫자)
                Integer exerciseServerId = exerciseIdMapper.toServerId(exerciseData.getExerciseId());
                Exercise exercise = exerciseRepository.findById(exerciseServerId)
                    .orElseGet(() -> {
                        // 운동이 없으면 기본 운동으로 처리 (또는 새로 생성)
                        log.warn("Exercise not found: {}, creating placeholder", exerciseData.getExerciseId());
                        return exerciseRepository.findById(9999) // 기타 운동
                            .orElse(null);
                    });

                if (exercise != null) {
                    WorkoutExercise workoutExercise = WorkoutExercise.builder()
                        .workout(workout)
                        .exercise(exercise)
                        .orderIndex(exerciseData.getOrder() != null ? exerciseData.getOrder() : 0)
                        .note(exerciseData.getNotes())
                        .build();

                    // 세트 데이터 저장
                    if (exerciseData.getSets() != null) {
                        for (WorkoutSessionResponse.SetData setData : exerciseData.getSets()) {
                            WorkoutSet workoutSet = WorkoutSet.builder()
                                .workoutExercise(workoutExercise)
                                .setNumber(setData.getSetNumber() != null ? setData.getSetNumber() : 1)
                                .weight(setData.getWeight())
                                .reps(setData.getReps())
                                .distance(setData.getDistance())
                                .duration(setData.getDuration())
                                .isWarmup(setData.getIsWarmup())
                                .isFailure(setData.getIsFailure())
                                .isCompleted(setData.getCompleted())
                                .build();

                            workoutExercise.addSet(workoutSet);
                        }
                    }

                    workout.addExercise(workoutExercise);
                }
            }
        }

        Workout savedWorkout = workoutRepository.save(workout);
        log.info("Workout session saved from frontend: {} for user: {}", savedWorkout.getId(), user.getEmail());

        // 저장된 운동을 다시 WorkoutSessionResponse로 변환하여 반환
        return convertToWorkoutSessionResponse(savedWorkout);
    }

    /**
     * 사용자 운동 히스토리 조회 (프론트엔드 규격)
     */
    @Transactional(readOnly = true)
    public List<WorkoutSessionResponse> getUserWorkoutHistory(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Workout> workouts = workoutRepository.findByUserOrderByStartTimeDesc(user);

        return workouts.stream()
            .map(this::convertToWorkoutSessionResponse)
            .collect(Collectors.toList());
    }

    /**
     * 마지막 운동 조회 (프론트엔드 규격)
     */
    @Transactional(readOnly = true)
    public WorkoutSessionResponse getLastWorkout(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Optional<Workout> lastWorkout = workoutRepository.findTopByUserOrderByStartTimeDesc(user);

        return lastWorkout.map(this::convertToWorkoutSessionResponse)
            .orElse(null);
    }

    /**
     * Workout 엔티티를 WorkoutSessionResponse로 변환
     */
    private WorkoutSessionResponse convertToWorkoutSessionResponse(Workout workout) {
        WorkoutSessionResponse response = WorkoutSessionResponse.builder()
            .id(workout.getId().toString())
            .userId(workout.getUser().getId().toString())
            .title(workout.getWorkoutName())
            .date(workout.getStartTime().toLocalDate())
            .startTime(workout.getStartTime())
            .endTime(workout.getEndTime())
            .totalSets(workout.getTotalSets())
            .totalVolume(workout.getTotalVolume())
            .duration(workout.getDuration() != null ? workout.getDuration() * 1000L : null) // seconds to milliseconds
            .caloriesBurned(workout.getCalories())
            .notes(workout.getNote())
            .status(workout.getStatus().toString().toLowerCase())
            .exercises(new ArrayList<>())
            .build();

        // 운동별 데이터 변환
        if (workout.getWorkoutExercises() != null) {
            for (WorkoutExercise we : workout.getWorkoutExercises()) {
                Exercise exercise = we.getExercise();

                WorkoutSessionResponse.WorkoutExerciseData exerciseData =
                    WorkoutSessionResponse.WorkoutExerciseData.builder()
                        .id(we.getId().toString())
                        .exerciseId(ExerciseIdMapper.toClientId(exercise.getId())) // 숫자 ID -> 문자열 ID
                        .name(exercise.getExerciseName())
                        .nameKo(exercise.getExerciseNameKo())
                        .category(exercise.getCategory().name().toLowerCase())
                        .muscleGroups(exercise.getMuscleGroups() != null ?
                            Arrays.asList(exercise.getMuscleGroups().split(",")) : new ArrayList<>())
                        .equipment(exercise.getEquipment())
                        .unit(exercise.getUnit().name().toLowerCase())
                        .order(we.getOrderIndex())
                        .notes(we.getNote())
                        .sets(new ArrayList<>())
                        .build();

                // 세트 데이터 변환
                if (we.getWorkoutSets() != null) {
                    for (WorkoutSet set : we.getWorkoutSets()) {
                        WorkoutSessionResponse.SetData setData =
                            WorkoutSessionResponse.SetData.builder()
                                .id(set.getId().toString())
                                .setNumber(set.getSetNumber())
                                .weight(set.getWeight())
                                .reps(set.getReps())
                                .distance(set.getDistance())
                                .duration(set.getDuration())
                                .level(set.getLevel())
                                .isWarmup(set.getIsWarmup())
                                .isFailure(set.getIsFailure())
                                .isDropset(set.getIsDropset())
                                .completed(set.getIsCompleted())
                                .completedAt(set.getCreatedAt())
                                .build();

                        exerciseData.getSets().add(setData);
                    }
                }

                response.getExercises().add(exerciseData);
            }
        }

        return response;
    }
}