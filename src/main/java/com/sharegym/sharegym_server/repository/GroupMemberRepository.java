package com.sharegym.sharegym_server.repository;

import com.sharegym.sharegym_server.entity.Group;
import com.sharegym.sharegym_server.entity.GroupMember;
import com.sharegym.sharegym_server.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 그룹 멤버 리포지토리
 */
@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    /**
     * 그룹과 사용자로 멤버 조회
     */
    Optional<GroupMember> findByGroupAndUser(Group group, User user);

    /**
     * 그룹 ID와 사용자 ID로 멤버 조회
     */
    @Query("SELECT gm FROM GroupMember gm " +
           "WHERE gm.group.id = :groupId " +
           "AND gm.user.id = :userId")
    Optional<GroupMember> findByGroupIdAndUserId(@Param("groupId") Long groupId,
                                                  @Param("userId") Long userId);

    /**
     * 그룹과 사용자 존재 여부 확인
     */
    boolean existsByGroupAndUser(Group group, User user);

    /**
     * 그룹 ID와 사용자 ID로 존재 여부 확인
     */
    @Query("SELECT COUNT(gm) > 0 FROM GroupMember gm " +
           "WHERE gm.group.id = :groupId " +
           "AND gm.user.id = :userId")
    boolean existsByGroupIdAndUserId(@Param("groupId") Long groupId,
                                      @Param("userId") Long userId);

    /**
     * 그룹별 멤버 목록 조회
     */
    List<GroupMember> findByGroupOrderByJoinedAtAsc(Group group);

    /**
     * 그룹별 멤버 목록 조회 (페이징)
     */
    Page<GroupMember> findByGroup(Group group, Pageable pageable);

    /**
     * 그룹 ID로 멤버 목록 조회
     */
    @Query("SELECT gm FROM GroupMember gm " +
           "WHERE gm.group.id = :groupId " +
           "ORDER BY gm.role DESC, gm.joinedAt ASC")
    List<GroupMember> findByGroupId(@Param("groupId") Long groupId);

    /**
     * 사용자별 그룹 멤버십 목록 조회
     */
    List<GroupMember> findByUserOrderByJoinedAtDesc(User user);

    /**
     * 그룹별 관리자 조회
     */
    @Query("SELECT gm FROM GroupMember gm " +
           "WHERE gm.group = :group " +
           "AND gm.role = 'ADMIN'")
    List<GroupMember> findAdminsByGroup(@Param("group") Group group);

    /**
     * 그룹별 멤버 수 조회
     */
    @Query("SELECT COUNT(gm) FROM GroupMember gm " +
           "WHERE gm.group.id = :groupId")
    long countByGroupId(@Param("groupId") Long groupId);

    /**
     * 사용자가 속한 그룹 수 조회
     */
    @Query("SELECT COUNT(gm) FROM GroupMember gm " +
           "WHERE gm.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 그룹 탈퇴 처리
     */
    void deleteByGroupAndUser(Group group, User user);

    /**
     * 기여도 순위별 멤버 조회
     */
    @Query("SELECT gm FROM GroupMember gm " +
           "WHERE gm.group = :group " +
           "ORDER BY gm.contributionScore DESC")
    List<GroupMember> findTopContributors(@Param("group") Group group, Pageable pageable);
}