package com.sharegym.sharegym_server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * 사용자 Entity
 */
@Entity
@Table(name = "users",
    indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_username", columnList = "username")
    },
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "username")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    // 신체 정보
    @Column
    private Double height; // 키 (cm)

    @Column
    private Double weight; // 체중 (kg)

    // 사용자 통계
    @Column(name = "workout_count")
    @Builder.Default
    private Integer workoutCount = 0;

    @Column(name = "follower_count")
    @Builder.Default
    private Integer followerCount = 0;

    @Column(name = "following_count")
    @Builder.Default
    private Integer followingCount = 0;

    @Column(name = "current_streak")
    @Builder.Default
    private Integer currentStreak = 0;

    @Column(name = "max_streak")
    @Builder.Default
    private Integer maxStreak = 0;

    // 계정 상태
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    // FCM 토큰 (푸시 알림용)
    @Column(name = "fcm_token")
    private String fcmToken;

    // 팔로우 관계 (자기 참조)
    @ManyToMany
    @JoinTable(
        name = "user_follows",
        joinColumns = @JoinColumn(name = "follower_id"),
        inverseJoinColumns = @JoinColumn(name = "following_id")
    )
    @Builder.Default
    private Set<User> following = new HashSet<>();

    @ManyToMany(mappedBy = "following")
    @Builder.Default
    private Set<User> followers = new HashSet<>();

    // 연관 관계 - 운동 기록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Workout> workouts = new HashSet<>();

    // 연관 관계 - 피드
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Feed> feeds = new HashSet<>();

    // 연관 관계 - 그룹 멤버십
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<GroupMember> groupMemberships = new HashSet<>();

    // 연관 관계 - 루틴
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Routine> routines = new HashSet<>();

    /**
     * 사용자 권한 Enum
     */
    public enum Role {
        USER, ADMIN, TRAINER
    }

    /**
     * 팔로우 메서드
     */
    public void follow(User user) {
        this.following.add(user);
        user.getFollowers().add(this);
        this.followingCount++;
        user.setFollowerCount(user.getFollowerCount() + 1);
    }

    /**
     * 언팔로우 메서드
     */
    public void unfollow(User user) {
        this.following.remove(user);
        user.getFollowers().remove(this);
        this.followingCount--;
        user.setFollowerCount(user.getFollowerCount() - 1);
    }

    /**
     * 운동 횟수 증가
     */
    public void incrementWorkoutCount() {
        this.workoutCount++;
    }

    /**
     * 스트릭 업데이트
     */
    public void updateStreak(int newStreak) {
        this.currentStreak = newStreak;
        if (newStreak > this.maxStreak) {
            this.maxStreak = newStreak;
        }
    }
}