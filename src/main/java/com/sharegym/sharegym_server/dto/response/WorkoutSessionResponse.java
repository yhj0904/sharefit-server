package com.sharegym.sharegym_server.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sharegym.sharegym_server.common.ExerciseIdMapper;
import com.sharegym.sharegym_server.entity.Workout;
import com.sharegym.sharegym_server.entity.WorkoutExercise;
import com.sharegym.sharegym_server.entity.WorkoutSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 프론트엔드 WorkoutSession 형식에 맞춘 응답 DTO
 * 운동 완료 후 프론트엔드에서 전송하는 전체 세션 데이터를 처리
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutSessionResponse {

    private String id;
    private String userId;
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime endTime;

    @Builder.Default
    private List<WorkoutExerciseData> exercises = new ArrayList<>();

    private Integer totalSets;
    private Integer totalVolume;
    private Long duration; // milliseconds
    private Integer caloriesBurned;
    private String notes;
    private String status; // "active", "completed", "cancelled"

    /**
     * 운동별 데이터
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkoutExerciseData {
        private String id;
        private String exerciseId; // 프론트엔드는 문자열 ID 사용 (예: "bench-press")
        private String name;
        private String nameKo;
        private String category;
        private List<String> muscleGroups;
        private String equipment;
        private String unit; // "kg", "km", "level", "reps"

        @Builder.Default
        private List<SetData> sets = new ArrayList<>();

        private String notes;
        private Integer restTime; // seconds
        private Integer order;
    }

    /**
     * 세트 데이터
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SetData {
        private String id;
        private Integer setNumber;
        private Double weight; // kg or null for bodyweight
        private Integer reps;
        private Double distance; // km for cardio
        private Integer duration; // seconds for time-based exercises
        private Integer level; // for machine difficulty
        private Boolean isWarmup;
        private Boolean isFailure;
        private Boolean isDropset;
        private Boolean completed;
        private String notes;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        private LocalDateTime completedAt;
    }

    /**
     * Map 형태의 데이터를 WorkoutSessionResponse로 변환
     */
    public static WorkoutSessionResponse fromMap(Map<String, Object> data) {
        WorkoutSessionResponse response = new WorkoutSessionResponse();

        // 기본 필드 매핑
        response.setId(String.valueOf(data.getOrDefault("id", "")));
        response.setUserId(String.valueOf(data.getOrDefault("userId", "")));
        response.setTitle(String.valueOf(data.getOrDefault("title", "운동")));
        response.setStatus(String.valueOf(data.getOrDefault("status", "completed")));

        // 숫자 필드
        if (data.containsKey("totalSets")) {
            response.setTotalSets(((Number) data.get("totalSets")).intValue());
        }
        if (data.containsKey("totalVolume")) {
            response.setTotalVolume(((Number) data.get("totalVolume")).intValue());
        }
        if (data.containsKey("duration")) {
            response.setDuration(((Number) data.get("duration")).longValue());
        }
        if (data.containsKey("caloriesBurned")) {
            response.setCaloriesBurned(((Number) data.get("caloriesBurned")).intValue());
        }

        // 날짜/시간 필드 - ISO 문자열로 전달될 수 있음
        if (data.containsKey("date")) {
            Object dateObj = data.get("date");
            if (dateObj instanceof String) {
                response.setDate(LocalDate.parse(((String) dateObj).substring(0, 10)));
            }
        }
        if (data.containsKey("startTime")) {
            Object startTimeObj = data.get("startTime");
            if (startTimeObj instanceof String) {
                response.setStartTime(LocalDateTime.parse(((String) startTimeObj).replace("Z", "")));
            }
        }
        if (data.containsKey("endTime")) {
            Object endTimeObj = data.get("endTime");
            if (endTimeObj instanceof String) {
                response.setEndTime(LocalDateTime.parse(((String) endTimeObj).replace("Z", "")));
            }
        }

        // 운동 리스트 변환
        if (data.containsKey("exercises") && data.get("exercises") instanceof List) {
            List<Map<String, Object>> exercisesList = (List<Map<String, Object>>) data.get("exercises");
            List<WorkoutExerciseData> exercises = new ArrayList<>();

            for (Map<String, Object> exerciseMap : exercisesList) {
                WorkoutExerciseData exerciseData = convertToExerciseData(exerciseMap);
                exercises.add(exerciseData);
            }

            response.setExercises(exercises);
        }

        return response;
    }

    /**
     * Map을 WorkoutExerciseData로 변환
     */
    private static WorkoutExerciseData convertToExerciseData(Map<String, Object> map) {
        WorkoutExerciseData data = new WorkoutExerciseData();

        data.setId(String.valueOf(map.getOrDefault("id", "")));
        data.setExerciseId(String.valueOf(map.getOrDefault("exerciseId", "")));
        data.setName(String.valueOf(map.getOrDefault("name", "")));
        data.setNameKo(String.valueOf(map.getOrDefault("nameKo", "")));
        data.setCategory(String.valueOf(map.getOrDefault("category", "")));
        data.setEquipment(String.valueOf(map.getOrDefault("equipment", "")));
        data.setUnit(String.valueOf(map.getOrDefault("unit", "kg")));

        if (map.containsKey("muscleGroups") && map.get("muscleGroups") instanceof List) {
            data.setMuscleGroups((List<String>) map.get("muscleGroups"));
        }

        if (map.containsKey("order")) {
            data.setOrder(((Number) map.get("order")).intValue());
        }
        if (map.containsKey("restTime")) {
            data.setRestTime(((Number) map.get("restTime")).intValue());
        }

        // 세트 리스트 변환
        if (map.containsKey("sets") && map.get("sets") instanceof List) {
            List<Map<String, Object>> setsList = (List<Map<String, Object>>) map.get("sets");
            List<SetData> sets = new ArrayList<>();

            for (Map<String, Object> setMap : setsList) {
                SetData setData = convertToSetData(setMap);
                sets.add(setData);
            }

            data.setSets(sets);
        }

        return data;
    }

    /**
     * Map을 SetData로 변환
     */
    private static SetData convertToSetData(Map<String, Object> map) {
        SetData data = new SetData();

        data.setId(String.valueOf(map.getOrDefault("id", "")));

        if (map.containsKey("setNumber")) {
            data.setSetNumber(((Number) map.get("setNumber")).intValue());
        }
        if (map.containsKey("weight")) {
            data.setWeight(((Number) map.get("weight")).doubleValue());
        }
        if (map.containsKey("reps")) {
            data.setReps(((Number) map.get("reps")).intValue());
        }
        if (map.containsKey("distance")) {
            data.setDistance(((Number) map.get("distance")).doubleValue());
        }
        if (map.containsKey("duration")) {
            data.setDuration(((Number) map.get("duration")).intValue());
        }
        if (map.containsKey("level")) {
            data.setLevel(((Number) map.get("level")).intValue());
        }

        // Boolean 필드들
        data.setIsWarmup(Boolean.TRUE.equals(map.get("isWarmup")));
        data.setIsFailure(Boolean.TRUE.equals(map.get("isFailure")));
        data.setIsDropset(Boolean.TRUE.equals(map.get("isDropset")));
        data.setCompleted(Boolean.TRUE.equals(map.get("completed")));

        data.setNotes(String.valueOf(map.getOrDefault("notes", "")));

        // completedAt 날짜
        if (map.containsKey("completedAt")) {
            Object completedAtObj = map.get("completedAt");
            if (completedAtObj instanceof String) {
                data.setCompletedAt(LocalDateTime.parse(((String) completedAtObj).replace("Z", "")));
            }
        }

        return data;
    }

    /**
     * Workout Entity를 WorkoutSessionResponse로 변환
     */
    public static WorkoutSessionResponse from(Workout workout) {
        WorkoutSessionResponse response = WorkoutSessionResponse.builder()
            .id(String.valueOf(workout.getId()))
            .userId(String.valueOf(workout.getUser().getId()))
            .title(workout.getWorkoutName() != null ? workout.getWorkoutName() : "운동")
            .date(workout.getStartTime().toLocalDate())
            .startTime(workout.getStartTime())
            .endTime(workout.getEndTime())
            .totalSets(workout.getTotalSets())
            .totalVolume(workout.getTotalVolume())
            .duration(workout.getDuration() != null ? workout.getDuration() * 1000L : null) // 초를 밀리초로 변환
            .caloriesBurned(workout.getCalories() != null ? workout.getCalories() : workout.getCaloriesBurned())
            .notes(workout.getNote())
            .status(workout.getStatus().name().toLowerCase())
            .exercises(new ArrayList<>())
            .build();

        // 운동별 데이터 변환
        if (workout.getExercises() != null) {
            List<WorkoutExerciseData> exerciseDataList = workout.getExercises().stream()
                .map(WorkoutSessionResponse::convertWorkoutExerciseToData)
                .collect(Collectors.toList());
            response.setExercises(exerciseDataList);
        }

        return response;
    }

    /**
     * WorkoutExercise Entity를 WorkoutExerciseData로 변환
     */
    private static WorkoutExerciseData convertWorkoutExerciseToData(WorkoutExercise workoutExercise) {
        WorkoutExerciseData data = WorkoutExerciseData.builder()
            .id(String.valueOf(workoutExercise.getId()))
            .exerciseId(ExerciseIdMapper.toClientId(workoutExercise.getExercise().getId())) // 숫자 ID를 문자열 ID로 변환
            .name(workoutExercise.getExercise().getName())
            .nameKo(workoutExercise.getExercise().getNameKo())
            .category(workoutExercise.getExercise().getCategory().name().toLowerCase())
            .muscleGroups(Arrays.asList(workoutExercise.getExercise().getMuscleGroups().split(",")))
            .equipment(workoutExercise.getExercise().getEquipment())
            .unit(workoutExercise.getExercise().getUnit().name().toLowerCase())
            .notes(workoutExercise.getNote())
            .order(workoutExercise.getOrderIndex())
            .sets(new ArrayList<>())
            .build();

        // 세트 데이터 변환
        if (workoutExercise.getSets() != null) {
            List<SetData> setDataList = workoutExercise.getSets().stream()
                .map(WorkoutSessionResponse::convertWorkoutSetToData)
                .collect(Collectors.toList());
            data.setSets(setDataList);
        }

        return data;
    }

    /**
     * WorkoutSet Entity를 SetData로 변환
     */
    private static SetData convertWorkoutSetToData(WorkoutSet workoutSet) {
        return SetData.builder()
            .id(String.valueOf(workoutSet.getId()))
            .setNumber(workoutSet.getSetNumber())
            .weight(workoutSet.getWeight())
            .reps(workoutSet.getReps())
            .distance(workoutSet.getDistance())
            .duration(workoutSet.getDuration())
            .level(workoutSet.getLevel())
            .isWarmup(workoutSet.getIsWarmup())
            .isFailure(workoutSet.getIsFailure())
            .isDropset(workoutSet.getIsDropset())
            .completed(workoutSet.getIsCompleted())
            .notes(workoutSet.getNote())
            .completedAt(workoutSet.getCompletedAt())
            .build();
    }
}