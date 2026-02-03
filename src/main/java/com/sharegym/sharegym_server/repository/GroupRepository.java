package com.sharegym.sharegym_server.repository;

import com.sharegym.sharegym_server.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 그룹 리포지토리
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    /**
     * 초대 코드로 그룹 조회
     */
    Optional<Group> findByInviteCode(String inviteCode);

    /**
     * 사용자가 속한 그룹 목록 조회
     * @param userId 사용자 ID
     * @return 그룹 목록
     */
    @Query("SELECT g FROM Group g " +
           "JOIN g.members m " +
           "WHERE m.user.id = :userId " +
           "AND g.isActive = true " +
           "ORDER BY g.createdAt DESC")
    List<Group> findByMembersUserId(@Param("userId") Long userId);

    /**
     * 사용자가 속한 그룹 목록 조회 (페이징)
     */
    @Query("SELECT g FROM Group g " +
           "JOIN g.members m " +
           "WHERE m.user.id = :userId " +
           "AND g.isActive = true " +
           "ORDER BY g.createdAt DESC")
    Page<Group> findByMembersUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 공개 그룹 목록 조회 (인기순)
     */
    Page<Group> findByIsPublicTrueAndIsActiveTrueOrderByMemberCountDesc(Pageable pageable);

    /**
     * 공개 그룹 목록 조회 (최신순)
     */
    Page<Group> findByIsPublicTrueAndIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 그룹명으로 검색
     */
    @Query("SELECT g FROM Group g " +
           "WHERE g.name LIKE %:keyword% " +
           "AND g.isActive = true " +
           "ORDER BY g.memberCount DESC")
    Page<Group> searchByName(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 활성 그룹 개수 조회
     */
    long countByIsActiveTrue();

    /**
     * 사용자가 관리자인 그룹 목록 조회
     */
    @Query("SELECT g FROM Group g " +
           "JOIN g.members m " +
           "WHERE m.user.id = :userId " +
           "AND m.role = 'ADMIN' " +
           "AND g.isActive = true " +
           "ORDER BY g.createdAt DESC")
    List<Group> findByAdminUserId(@Param("userId") Long userId);

    /**
     * 초대 코드 중복 확인
     */
    boolean existsByInviteCode(String inviteCode);

    /**
     * 그룹 멤버 수 업데이트
     */
    @Query("UPDATE Group g SET g.memberCount = " +
           "(SELECT COUNT(m) FROM GroupMember m WHERE m.group = g) " +
           "WHERE g.id = :groupId")
    void updateMemberCount(@Param("groupId") Long groupId);
}