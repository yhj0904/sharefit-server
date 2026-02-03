package com.sharegym.sharegym_server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 피드 게시물 Entity
 */
@Entity
@Table(name = "feeds",
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feed extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id")
    private Workout workout; // 연결된 운동 세션 (선택적)

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 게시물 내용

    @Column(name = "image_url")
    private String imageUrl; // 운동 카드 이미지 URL (단일 이미지, 하위 호환성)

    @ElementCollection
    @CollectionTable(name = "feed_images", joinColumns = @JoinColumn(name = "feed_id"))
    @Column(name = "image_url")
    @OrderColumn(name = "image_order")
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>(); // 다중 이미지 URL

    @Enumerated(EnumType.STRING)
    @Column(name = "card_style", length = 20)
    private CardStyle cardStyle; // 운동 카드 스타일

    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "comment_count")
    @Builder.Default
    private Integer commentCount = 0;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    // 연관 관계 - 좋아요
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<FeedLike> likes = new HashSet<>();

    // 연관 관계 - 댓글
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<FeedComment> comments = new ArrayList<>();

    // 연관 관계 - 그룹 공유
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group sharedGroup; // 공유된 그룹 (선택적)

    /**
     * 운동 카드 스타일 Enum
     */
    public enum CardStyle {
        MINIMAL,    // 미니멀
        GRADIENT,   // 그라데이션
        DARK,       // 다크
        COLORFUL    // 컬러풀
    }

    /**
     * 좋아요 추가
     */
    public void addLike(User user) {
        FeedLike like = FeedLike.builder()
            .feed(this)
            .user(user)
            .build();
        likes.add(like);
        this.likeCount++;
    }

    /**
     * 좋아요 제거
     */
    public void removeLike(User user) {
        likes.removeIf(like -> like.getUser().getId().equals(user.getId()));
        this.likeCount = Math.max(0, this.likeCount - 1);
    }

    /**
     * 댓글 추가
     */
    public void addComment(FeedComment comment) {
        comments.add(comment);
        comment.setFeed(this);
        this.commentCount++;
    }

    /**
     * 댓글 제거
     */
    public void removeComment(FeedComment comment) {
        comments.remove(comment);
        comment.setFeed(null);
        this.commentCount = Math.max(0, this.commentCount - 1);
    }

    /**
     * 사용자가 좋아요를 눌렀는지 확인
     */
    public boolean isLikedByUser(Long userId) {
        return likes.stream()
            .anyMatch(like -> like.getUser().getId().equals(userId));
    }

    /**
     * 소프트 삭제
     */
    public void softDelete() {
        this.isDeleted = true;
    }

    /**
     * 편의 메서드 - 좋아요 수 반환
     */
    public Integer getLikesCount() {
        return this.likeCount;
    }

    /**
     * 편의 메서드 - 댓글 수 반환
     */
    public Integer getCommentsCount() {
        return this.commentCount;
    }
}