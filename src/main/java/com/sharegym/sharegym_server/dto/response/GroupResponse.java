package com.sharegym.sharegym_server.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharegym.sharegym_server.entity.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 그룹 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupResponse {

    private Long id;
    private String name;
    private String description;
    private String profileImageUrl;
    private String inviteCode;
    private Integer memberCount;
    private Integer maxMembers;
    private Boolean isPublic;
    private Boolean isActive;
    private Boolean isMember;  // 현재 사용자가 멤버인지
    private Boolean isAdmin;   // 현재 사용자가 관리자인지
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity로부터 DTO 생성
     */
    public static GroupResponse from(Group group, boolean isMember) {
        return GroupResponse.builder()
            .id(group.getId())
            .name(group.getName())
            .description(group.getDescription())
            .profileImageUrl(group.getProfileImageUrl())
            .inviteCode(group.getInviteCode())
            .memberCount(group.getMemberCount())
            .maxMembers(group.getMaxMembers())
            .isPublic(group.getIsPublic())
            .isActive(group.getIsActive())
            .isMember(isMember)
            .createdAt(group.getCreatedAt())
            .updatedAt(group.getUpdatedAt())
            .build();
    }

    /**
     * Entity로부터 DTO 생성 (관리자 정보 포함)
     */
    public static GroupResponse from(Group group, boolean isMember, boolean isAdmin) {
        GroupResponse response = from(group, isMember);
        response.setIsAdmin(isAdmin);
        return response;
    }
}