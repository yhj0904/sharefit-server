package com.sharegym.sharegym_server.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharegym.sharegym_server.entity.Feed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 그룹 포스트 응답 DTO
 * Frontend의 GroupPost 타입과 호환
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupPostResponse {

    private Long id;
    private Long userId;
    private String username;
    private String userDisplayName;
    private String userProfileImage;
    private String content;
    private String imageUrl;
    private Long workoutId;
    private WorkoutSessionResponse workoutSnapshot;  // 운동 세션 스냅샷
    private String cardStyle;
    private Integer likesCount;
    private Integer commentsCount;
    private Boolean isLiked;
    private List<CommentResponse> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity로부터 DTO 생성
     */
    public static GroupPostResponse from(Feed feed) {
        return GroupPostResponse.builder()
            .id(feed.getId())
            .userId(feed.getUser().getId())
            .username(feed.getUser().getUsername())
            .userDisplayName(feed.getUser().getDisplayName())
            .userProfileImage(feed.getUser().getProfileImageUrl())
            .content(feed.getContent())
            .imageUrl(feed.getImageUrl())
            .workoutId(feed.getWorkout() != null ? feed.getWorkout().getId() : null)
            .workoutSnapshot(feed.getWorkout() != null ?
                WorkoutSessionResponse.from(feed.getWorkout()) : null)
            .cardStyle(feed.getCardStyle() != null ? feed.getCardStyle().name() : null)
            .likesCount(feed.getLikesCount())
            .commentsCount(feed.getCommentsCount())
            .createdAt(feed.getCreatedAt())
            .updatedAt(feed.getUpdatedAt())
            .build();
    }

    /**
     * Entity로부터 DTO 생성 (댓글 포함)
     */
    public static GroupPostResponse fromWithComments(Feed feed) {
        GroupPostResponse response = from(feed);
        if (feed.getComments() != null) {
            response.setComments(feed.getComments().stream()
                .filter(comment -> !comment.getIsDeleted())
                .map(CommentResponse::from)
                .collect(Collectors.toList()));
        }
        return response;
    }

    /**
     * Entity로부터 DTO 생성 (좋아요 상태 포함)
     */
    public static GroupPostResponse from(Feed feed, boolean isLiked) {
        GroupPostResponse response = from(feed);
        response.setIsLiked(isLiked);
        return response;
    }
}