package com.sharegym.sharegym_server.repository;

import com.sharegym.sharegym_server.entity.User;
import com.sharegym.sharegym_server.entity.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Workout Repository
 */
@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    /**
     * 사용자의 운동 목록 조회 (페이징)
     */
    Page<Workout> findByUserOrderByStartTimeDesc(User user, Pageable pageable);

    /**
     * 사용자의 모든 운동 목록 조회
     */
    List<Workout> findByUserOrderByStartTimeDesc(User user);

    /**
     * 사용자의 운동 목록 조회 (기간별)
     */
    List<Workout> findByUserAndStartTimeBetweenOrderByStartTimeDesc(
        User user,
        LocalDateTime startTime,
        LocalDateTime endTime
    );

    /**
     * 사용자의 최근 운동 조회
     */
    Optional<Workout> findTopByUserOrderByStartTimeDesc(User user);

    /**
     * 사용자의 진행 중인 운동 조회
     */
    @Query("SELECT w FROM Workout w WHERE w.user = :user AND w.status = 'IN_PROGRESS'")
    Optional<Workout> findInProgressWorkout(@Param("user") User user);

    /**
     * 사용자의 완료된 운동 수
     */
    @Query("SELECT COUNT(w) FROM Workout w WHERE w.user = :user AND w.status = 'COMPLETED'")
    Long countCompletedWorkouts(@Param("user") User user);

    /**
     * 사용자의 특정 기간 동안의 총 운동 시간 (분)
     */
    @Query("SELECT SUM(w.durationMinutes) FROM Workout w WHERE w.user = :user " +
           "AND w.status = 'COMPLETED' AND w.startTime BETWEEN :startTime AND :endTime")
    Integer getTotalDurationMinutes(
        @Param("user") User user,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 사용자의 특정 기간 동안의 총 볼륨 (kg)
     */
    @Query("SELECT SUM(w.totalWeight) FROM Workout w WHERE w.user = :user " +
           "AND w.status = 'COMPLETED' AND w.startTime BETWEEN :startTime AND :endTime")
    Double getTotalVolume(
        @Param("user") User user,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}