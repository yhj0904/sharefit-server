package com.sharegym.sharegym_server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 그룹 Entity
 */
@Entity
@Table(name = "`user_groups`", // MySQL 예약어 회피
    indexes = {
        @Index(name = "idx_invite_code", columnList = "invite_code"),
        @Index(name = "idx_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 그룹 이름

    @Column(columnDefinition = "TEXT")
    private String description; // 그룹 설명

    @Column(name = "profile_image_url")
    private String profileImageUrl; // 그룹 프로필 이미지

    @Column(name = "invite_code", unique = true, length = 6)
    private String inviteCode; // 초대 코드 (6자리 영문+숫자)

    @Column(name = "member_count")
    @Builder.Default
    private Integer memberCount = 0;

    @Column(name = "max_members")
    @Builder.Default
    private Integer maxMembers = 100; // 최대 멤버 수

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false; // 공개 여부

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // 연관 관계 - 그룹 멤버
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroupMember> members = new ArrayList<>();

    // 연관 관계 - 그룹에 공유된 피드
    @OneToMany(mappedBy = "sharedGroup")
    @OrderBy("createdAt DESC")
    @Builder.Default
    private List<Feed> sharedFeeds = new ArrayList<>();

    /**
     * 초대 코드 생성 (6자리 영문+숫자)
     */
    @PrePersist
    public void generateInviteCode() {
        if (this.inviteCode == null) {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder code = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 6; i++) {
                code.append(chars.charAt(random.nextInt(chars.length())));
            }
            this.inviteCode = code.toString();
        }
    }

    /**
     * 멤버 추가
     */
    public void addMember(GroupMember member) {
        members.add(member);
        member.setGroup(this);
        this.memberCount++;
    }

    /**
     * 멤버 제거
     */
    public void removeMember(GroupMember member) {
        members.remove(member);
        member.setGroup(null);
        this.memberCount = Math.max(0, this.memberCount - 1);
    }

    /**
     * 그룹이 가득 찼는지 확인
     */
    public boolean isFull() {
        return memberCount >= maxMembers;
    }

    /**
     * 사용자가 멤버인지 확인
     */
    public boolean hasMember(Long userId) {
        return members.stream()
            .anyMatch(member -> member.getUser().getId().equals(userId));
    }

    /**
     * 사용자가 관리자인지 확인
     */
    public boolean isAdmin(Long userId) {
        return members.stream()
            .anyMatch(member ->
                member.getUser().getId().equals(userId) &&
                member.getRole() == GroupMember.MemberRole.ADMIN
            );
    }
}