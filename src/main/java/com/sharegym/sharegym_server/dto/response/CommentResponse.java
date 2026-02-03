package com.sharegym.sharegym_server.dto.response;

import com.sharegym.sharegym_server.entity.FeedComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 댓글 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private Long feedId;
    private Long userId;
    private String userName;
    private String userProfileImage;
    private String content;
    private Long parentCommentId;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity를 DTO로 변환
     */
    public static CommentResponse from(FeedComment comment) {
        return CommentResponse.builder()
            .id(comment.getId())
            .feedId(comment.getFeed().getId())
            .userId(comment.getUser().getId())
            .userName(comment.getUser().getDisplayName())
            .userProfileImage(comment.getUser().getProfileImageUrl())
            .content(comment.getContent())
            .parentCommentId(comment.getParentComment() != null ?
                comment.getParentComment().getId() : null)
            .isDeleted(comment.getIsDeleted())
            .createdAt(comment.getCreatedAt())
            .updatedAt(comment.getUpdatedAt())
            .build();
    }
}