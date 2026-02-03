package com.sharegym.sharegym_server.dto.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 좋아요 알림 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LikeNotification {
    private Long feedId;
    private Long likerId;
    private String likerName;
    private String likerProfileImage;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}