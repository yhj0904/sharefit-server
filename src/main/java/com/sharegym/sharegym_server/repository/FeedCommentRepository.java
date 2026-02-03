package com.sharegym.sharegym_server.repository;

import com.sharegym.sharegym_server.entity.Feed;
import com.sharegym.sharegym_server.entity.FeedComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * FeedComment Repository
 */
@Repository
public interface FeedCommentRepository extends JpaRepository<FeedComment, Long> {

    /**
     * 특정 피드의 댓글 목록 조회
     */
    List<FeedComment> findByFeedAndIsDeletedFalseOrderByCreatedAtAsc(Feed feed);

    /**
     * 특정 피드의 댓글 목록 조회 (페이징)
     */
    Page<FeedComment> findByFeedAndIsDeletedFalseOrderByCreatedAtAsc(Feed feed, Pageable pageable);

    /**
     * 특정 피드의 댓글 개수
     */
    long countByFeedAndIsDeletedFalse(Feed feed);
}