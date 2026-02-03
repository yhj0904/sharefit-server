package com.sharegym.sharegym_server.repository;

import com.sharegym.sharegym_server.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Exercise Repository
 */
@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Integer> {

    /**
     * 클라이언트 ID로 운동 조회
     */
    Optional<Exercise> findByClientId(String clientId);

    /**
     * 카테고리별 운동 목록 조회
     */
    List<Exercise> findByCategory(Exercise.Category category);

    /**
     * 활성 운동 목록 조회
     */
    List<Exercise> findByIsActiveTrue();

    /**
     * 카테고리별 활성 운동 목록 조회
     */
    List<Exercise> findByCategoryAndIsActiveTrue(Exercise.Category category);

    /**
     * 클라이언트 ID 존재 여부 확인
     */
    boolean existsByClientId(String clientId);
}