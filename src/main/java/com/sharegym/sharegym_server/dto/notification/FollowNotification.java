package com.sharegym.sharegym_server.dto.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 팔로우 알림 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FollowNotification {
    private Long followerId;
    private String followerName;
    private String followerProfileImage;
    private String followerBio;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}