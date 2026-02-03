package com.sharegym.sharegym_server.dto.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 응원 알림 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheerNotification {
    private Long fromUserId;
    private String fromUserName;
    private String fromUserProfileImage;
    private String message;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}