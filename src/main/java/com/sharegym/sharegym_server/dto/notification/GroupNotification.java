package com.sharegym.sharegym_server.dto.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 그룹 알림 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupNotification {
    private Long groupId;
    private String groupName;
    private Long userId;
    private String userName;
    private String eventType; // MEMBER_JOIN, MEMBER_LEAVE, GROUP_UPDATE
    private String message;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}