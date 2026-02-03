package com.sharegym.sharegym_server.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 피드 좋아요 Entity
 */
@Entity
@Table(name = "feed_likes",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"feed_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_feed_id", columnList = "feed_id"),
        @Index(name = "idx_user_id", columnList = "user_id")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}