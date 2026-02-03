package com.sharegym.sharegym_server.dto.response;

import com.sharegym.sharegym_server.entity.Feed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 피드 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userProfileImage;
    private Long workoutId;
    private String content;
    private String imageUrl;
    private String cardStyle;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isLiked; // 현재 사용자가 좋아요를 눌렀는지
    private Long sharedGroupId;
    private String sharedGroupName;
    private List<CommentResponse> comments; // 댓글 목록 (선택적)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity를 DTO로 변환
     */
    public static FeedResponse from(Feed feed) {
        return FeedResponse.builder()
            .id(feed.getId())
            .userId(feed.getUser().getId())
            .userName(feed.getUser().getDisplayName())
            .userProfileImage(feed.getUser().getProfileImageUrl())
            .workoutId(feed.getWorkout() != null ? feed.getWorkout().getId() : null)
            .content(feed.getContent())
            .imageUrl(feed.getImageUrl())
            .cardStyle(feed.getCardStyle() != null ? feed.getCardStyle().name() : null)
            .likeCount(feed.getLikeCount())
            .commentCount(feed.getCommentCount())
            .sharedGroupId(feed.getSharedGroup() != null ? feed.getSharedGroup().getId() : null)
            .sharedGroupName(feed.getSharedGroup() != null ? feed.getSharedGroup().getName() : null)
            .createdAt(feed.getCreatedAt())
            .updatedAt(feed.getUpdatedAt())
            .build();
    }

    /**
     * Entity를 DTO로 변환 (좋아요 상태 포함)
     */
    public static FeedResponse from(Feed feed, boolean isLiked) {
        FeedResponse response = from(feed);
        response.isLiked = isLiked;
        return response;
    }

    /**
     * Entity를 DTO로 변환 (댓글 포함)
     */
    public static FeedResponse fromWithComments(Feed feed, boolean isLiked) {
        FeedResponse response = from(feed, isLiked);
        response.comments = feed.getComments().stream()
            .filter(comment -> !comment.getIsDeleted())
            .map(CommentResponse::from)
            .collect(Collectors.toList());
        return response;
    }

    /**
     * 편의 메서드 - 현재 사용자가 좋아요를 눌렀는지 확인
     */
    public Boolean getIsLikedByMe() {
        return this.isLiked;
    }

    /**
     * 편의 메서드 - 좋아요 수 반환
     */
    public Integer getLikesCount() {
        return this.likeCount;
    }
}