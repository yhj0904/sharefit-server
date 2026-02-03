package com.sharegym.sharegym_server.dto.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 피드 알림 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeedNotification {
    private Long feedId;
    private Long userId;
    private String userName;
    private String userProfileImage;
    private String content;
    private Boolean hasWorkout;
    private Long workoutId;
    private String imageUrl;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}