package com.sharegym.sharegym_server.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharegym.sharegym_server.entity.GroupMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 그룹 멤버 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupMemberResponse {

    private Long id;
    private Long userId;
    private String username;
    private String displayName;
    private String profileImageUrl;
    private String role;  // ADMIN, MODERATOR, MEMBER
    private LocalDateTime joinedAt;
    private Integer contributionScore;

    /**
     * Entity로부터 DTO 생성
     */
    public static GroupMemberResponse from(GroupMember member) {
        return GroupMemberResponse.builder()
            .id(member.getId())
            .userId(member.getUser().getId())
            .username(member.getUser().getUsername())
            .displayName(member.getUser().getDisplayName())
            .profileImageUrl(member.getUser().getProfileImageUrl())
            .role(member.getRole().name())
            .joinedAt(member.getJoinedAt())
            .contributionScore(member.getContributionScore())
            .build();
    }
}