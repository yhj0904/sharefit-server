package com.sharegym.sharegym_server.dto.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 댓글 알림 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentNotification {
    private Long feedId;
    private Long commentId;
    private Long commenterId;
    private String commenterName;
    private String commenterProfileImage;
    private String content;
    private Boolean isReply;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}