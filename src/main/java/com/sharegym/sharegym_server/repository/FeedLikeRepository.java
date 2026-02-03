package com.sharegym.sharegym_server.repository;

import com.sharegym.sharegym_server.entity.Feed;
import com.sharegym.sharegym_server.entity.FeedLike;
import com.sharegym.sharegym_server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * FeedLike Repository
 */
@Repository
public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {

    /**
     * 특정 피드에 사용자가 좋아요를 눌렀는지 확인
     */
    Optional<FeedLike> findByFeedAndUser(Feed feed, User user);

    /**
     * 특정 피드에 사용자가 좋아요를 눌렀는지 여부
     */
    boolean existsByFeedAndUser(Feed feed, User user);

    /**
     * 특정 피드에 사용자가 좋아요 삭제
     */
    void deleteByFeedAndUser(Feed feed, User user);
}