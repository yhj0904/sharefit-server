package com.sharegym.sharegym_server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 그룹 멤버 Entity
 */
@Entity
@Table(name = "group_members",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_group_id", columnList = "group_id"),
        @Index(name = "idx_user_id", columnList = "user_id")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MemberRole role = MemberRole.MEMBER;

    @Column(name = "joined_at", nullable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(name = "contribution_score")
    @Builder.Default
    private Integer contributionScore = 0; // 기여도 점수

    /**
     * 멤버 역할 Enum
     */
    public enum MemberRole {
        ADMIN,      // 관리자 (그룹 생성자)
        MODERATOR,  // 운영진
        MEMBER      // 일반 멤버
    }

    /**
     * 기여도 점수 증가
     */
    public void increaseContribution(int points) {
        this.contributionScore += points;
    }
}