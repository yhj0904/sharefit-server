package com.sharegym.sharegym_server.service;

import com.sharegym.sharegym_server.dto.request.CreateCommentRequest;
import com.sharegym.sharegym_server.dto.request.CreateFeedRequest;
import com.sharegym.sharegym_server.dto.response.CommentResponse;
import com.sharegym.sharegym_server.dto.response.FeedResponse;
import com.sharegym.sharegym_server.entity.*;
import com.sharegym.sharegym_server.exception.BusinessException;
import com.sharegym.sharegym_server.exception.ErrorCode;
import com.sharegym.sharegym_server.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 피드 관련 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedCommentRepository feedCommentRepository;
    private final UserRepository userRepository;
    private final WorkoutRepository workoutRepository;
    private final NotificationService notificationService;

    /**
     * 피드 생성
     */
    @Transactional
    public FeedResponse createFeed(Long userId, CreateFeedRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 운동 세션 연결 (선택적)
        Workout workout = null;
        if (request.getWorkoutId() != null) {
            workout = workoutRepository.findById(request.getWorkoutId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKOUT_NOT_FOUND));

            // 본인 운동인지 확인
            if (!workout.getUser().getId().equals(userId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED,
                    "본인의 운동만 피드에 공유할 수 있습니다.");
            }
        }

        // 그룹 연결 (선택적)
        Group sharedGroup = null;
        if (request.getGroupId() != null) {
            // TODO: GroupRepository 구현 후 처리
        }

        Feed feed = Feed.builder()
            .user(user)
            .workout(workout)
            .content(request.getContent())
            .imageUrl(request.getImageUrl())
            .cardStyle(request.getCardStyle())
            .sharedGroup(sharedGroup)
            .build();

        Feed savedFeed = feedRepository.save(feed);
        log.info("Feed created: {} by user: {}", savedFeed.getId(), user.getEmail());

        // SSE 알림 전송
        notificationService.notifyNewFeed(savedFeed);

        return FeedResponse.from(savedFeed, false);
    }

    /**
     * 피드 조회 (단일)
     */
    @Transactional(readOnly = true)
    public FeedResponse getFeed(Long feedId, Long userId) {
        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FEED_NOT_FOUND));

        if (feed.getIsDeleted()) {
            throw new BusinessException(ErrorCode.FEED_NOT_FOUND, "삭제된 피드입니다.");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean isLiked = feedLikeRepository.existsByFeedAndUser(feed, user);

        return FeedResponse.fromWithComments(feed, isLiked);
    }

    /**
     * 전체 피드 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<FeedResponse> getAllFeeds(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Page<Feed> feeds = feedRepository.findByIsDeletedFalseOrderByCreatedAtDesc(pageable);

        return feeds.map(feed -> {
            boolean isLiked = feedLikeRepository.existsByFeedAndUser(feed, user);
            return FeedResponse.from(feed, isLiked);
        });
    }

    /**
     * 팔로우하는 사용자들의 피드 조회
     */
    @Transactional(readOnly = true)
    public Page<FeedResponse> getFollowingFeeds(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 팔로우하는 사용자 목록 + 본인
        List<User> followingUsers = new ArrayList<>(user.getFollowing());
        followingUsers.add(user);

        Page<Feed> feeds = feedRepository.findByUsersOrderByCreatedAtDesc(followingUsers, pageable);

        return feeds.map(feed -> {
            boolean isLiked = feedLikeRepository.existsByFeedAndUser(feed, user);
            return FeedResponse.from(feed, isLiked);
        });
    }

    /**
     * 사용자의 피드 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<FeedResponse> getUserFeeds(Long targetUserId, Long currentUserId, Pageable pageable) {
        User targetUser = userRepository.findById(targetUserId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Page<Feed> feeds = feedRepository.findByUserAndIsDeletedFalseOrderByCreatedAtDesc(
            targetUser, pageable);

        return feeds.map(feed -> {
            boolean isLiked = feedLikeRepository.existsByFeedAndUser(feed, currentUser);
            return FeedResponse.from(feed, isLiked);
        });
    }

    /**
     * 피드 삭제
     */
    @Transactional
    public void deleteFeed(Long userId, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FEED_NOT_FOUND));

        // 권한 확인
        if (!feed.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED,
                "본인의 피드만 삭제할 수 있습니다.");
        }

        feed.softDelete();
        feedRepository.save(feed);

        log.info("Feed deleted: {} by user: {}", feedId, userId);
    }

    /**
     * 좋아요
     */
    @Transactional
    public void likeFeed(Long userId, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FEED_NOT_FOUND));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이미 좋아요를 눌렀는지 확인
        if (feedLikeRepository.existsByFeedAndUser(feed, user)) {
            throw new BusinessException(ErrorCode.ALREADY_LIKED);
        }

        feed.addLike(user);
        feedRepository.save(feed);

        // SSE 알림 전송
        notificationService.notifyFeedLike(feed, user);

        log.info("User {} liked feed {}", userId, feedId);
    }

    /**
     * 좋아요 취소
     */
    @Transactional
    public void unlikeFeed(Long userId, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FEED_NOT_FOUND));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 좋아요를 눌렀는지 확인
        FeedLike feedLike = feedLikeRepository.findByFeedAndUser(feed, user)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_LIKED));

        feed.removeLike(user);
        feedLikeRepository.delete(feedLike);
        feedRepository.save(feed);

        log.info("User {} unliked feed {}", userId, feedId);
    }

    /**
     * 댓글 작성
     */
    @Transactional
    public CommentResponse createComment(Long userId, Long feedId, CreateCommentRequest request) {
        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FEED_NOT_FOUND));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 부모 댓글 확인 (대댓글인 경우)
        FeedComment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = feedCommentRepository.findById(request.getParentCommentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

            // 부모 댓글이 같은 피드의 댓글인지 확인
            if (!parentComment.getFeed().getId().equals(feedId)) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "잘못된 부모 댓글입니다.");
            }
        }

        FeedComment comment = FeedComment.builder()
            .feed(feed)
            .user(user)
            .content(request.getContent())
            .parentComment(parentComment)
            .build();

        feed.addComment(comment);
        FeedComment savedComment = feedCommentRepository.save(comment);
        feedRepository.save(feed);

        // SSE 알림 전송
        notificationService.notifyFeedComment(feed, savedComment);

        log.info("Comment created on feed {} by user {}", feedId, userId);

        return CommentResponse.from(savedComment);
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long userId, Long feedId, Long commentId) {
        FeedComment comment = feedCommentRepository.findById(commentId)
            .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // 권한 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED,
                "본인의 댓글만 삭제할 수 있습니다.");
        }

        // 피드 확인
        if (!comment.getFeed().getId().equals(feedId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                "잘못된 피드 ID입니다.");
        }

        comment.softDelete();
        feedCommentRepository.save(comment);

        log.info("Comment {} deleted by user {}", commentId, userId);
    }

    /**
     * 피드의 댓글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getFeedComments(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new BusinessException(ErrorCode.FEED_NOT_FOUND));

        List<FeedComment> comments = feedCommentRepository
            .findByFeedAndIsDeletedFalseOrderByCreatedAtAsc(feed);

        return comments.stream()
            .map(CommentResponse::from)
            .collect(Collectors.toList());
    }
}