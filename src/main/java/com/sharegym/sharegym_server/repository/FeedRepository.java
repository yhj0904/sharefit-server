package com.sharegym.sharegym_server.repository;

import com.sharegym.sharegym_server.entity.Feed;
import com.sharegym.sharegym_server.entity.Group;
import com.sharegym.sharegym_server.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Feed Repository
 */
@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    /**
     * 사용자의 피드 목록 조회
     */
    Page<Feed> findByUserAndIsDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * 전체 피드 조회 (삭제되지 않은 것만)
     */
    Page<Feed> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 팔로우하는 사용자들의 피드 조회
     */
    @Query("SELECT f FROM Feed f WHERE f.user IN :users AND f.isDeleted = false ORDER BY f.createdAt DESC")
    Page<Feed> findByUsersOrderByCreatedAtDesc(@Param("users") List<User> users, Pageable pageable);

    /**
     * 그룹 피드 조회
     */
    @Query("SELECT f FROM Feed f WHERE f.sharedGroup.id = :groupId AND f.isDeleted = false ORDER BY f.createdAt DESC")
    Page<Feed> findByGroupIdOrderByCreatedAtDesc(@Param("groupId") Long groupId, Pageable pageable);

    /**
     * 사용자의 피드 개수
     */
    long countByUserAndIsDeletedFalse(User user);

    /**
     * 그룹에 공유된 피드 조회 (삭제되지 않은 것만)
     */
    Page<Feed> findBySharedGroupAndIsDeletedFalse(Group group, Pageable pageable);
}