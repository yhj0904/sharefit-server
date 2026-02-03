package com.sharegym.sharegym_server.service;

import com.sharegym.sharegym_server.dto.request.CreateGroupRequest;
import com.sharegym.sharegym_server.dto.request.JoinGroupRequest;
import com.sharegym.sharegym_server.dto.request.ShareToGroupRequest;
import com.sharegym.sharegym_server.dto.response.GroupResponse;
import com.sharegym.sharegym_server.dto.response.GroupMemberResponse;
import com.sharegym.sharegym_server.dto.response.GroupPostResponse;
import com.sharegym.sharegym_server.entity.*;
import com.sharegym.sharegym_server.exception.BusinessException;
import com.sharegym.sharegym_server.exception.ErrorCode;
import com.sharegym.sharegym_server.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 그룹 관련 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final WorkoutRepository workoutRepository;
    private final NotificationService notificationService;

    /**
     * 그룹 생성
     */
    @Transactional
    public GroupResponse createGroup(Long userId, CreateGroupRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 초대 코드 생성 (중복 방지)
        String inviteCode = generateUniqueInviteCode();

        Group group = Group.builder()
            .name(request.getName())
            .description(request.getDescription())
            .isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
            .profileImageUrl(request.getProfileImageUrl())
            .inviteCode(inviteCode)
            .memberCount(1)  // 생성자 포함
            .maxMembers(request.getMaxMembers() != null ? request.getMaxMembers() : 100)
            .build();

        Group savedGroup = groupRepository.save(group);

        // 생성자를 관리자로 추가
        GroupMember creator = GroupMember.builder()
            .group(savedGroup)
            .user(user)
            .role(GroupMember.MemberRole.ADMIN)
            .build();

        groupMemberRepository.save(creator);
        savedGroup.getMembers().add(creator);

        log.info("Group created: {} by user: {}", savedGroup.getId(), user.getEmail());

        return GroupResponse.from(savedGroup, true);  // 생성자는 당연히 멤버
    }

    /**
     * 초대 코드로 그룹 가입
     */
    @Transactional
    public GroupResponse joinGroup(Long userId, JoinGroupRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Group group = groupRepository.findByInviteCode(request.getInviteCode())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE_CODE));

        // 이미 가입했는지 확인
        if (groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new BusinessException(ErrorCode.ALREADY_GROUP_MEMBER);
        }

        // 그룹이 가득 찼는지 확인
        if (group.isFull()) {
            throw new BusinessException(ErrorCode.GROUP_FULL);
        }

        // 그룹 멤버 추가
        GroupMember member = GroupMember.builder()
            .group(group)
            .user(user)
            .role(GroupMember.MemberRole.MEMBER)
            .build();

        groupMemberRepository.save(member);
        group.addMember(member);
        groupRepository.save(group);

        // SSE 알림 전송
        notificationService.notifyGroupJoin(group, user);

        log.info("User {} joined group {} with invite code: {}",
                 userId, group.getId(), request.getInviteCode());

        return GroupResponse.from(group, true);
    }

    /**
     * 그룹 탈퇴
     */
    @Transactional
    public void leaveGroup(Long userId, Long groupId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        GroupMember member = groupMemberRepository.findByGroupAndUser(group, user)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));

        // 관리자가 마지막 멤버인 경우 탈퇴 불가
        if (member.getRole() == GroupMember.MemberRole.ADMIN &&
            group.getMemberCount() > 1) {

            // 다른 관리자가 있는지 확인
            List<GroupMember> admins = groupMemberRepository.findAdminsByGroup(group);
            if (admins.size() == 1) {
                throw new BusinessException(ErrorCode.LAST_ADMIN_CANNOT_LEAVE);
            }
        }

        // 멤버 삭제
        groupMemberRepository.delete(member);
        group.removeMember(member);
        groupRepository.save(group);

        log.info("User {} left group {}", userId, groupId);
    }

    /**
     * 사용자가 속한 그룹 목록 조회
     */
    @Transactional(readOnly = true)
    public List<GroupResponse> getUserGroups(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Group> groups = groupRepository.findByMembersUserId(userId);

        return groups.stream()
            .map(group -> GroupResponse.from(group, true))  // 이미 멤버
            .collect(Collectors.toList());
    }

    /**
     * 그룹 상세 조회
     */
    @Transactional(readOnly = true)
    public GroupResponse getGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        boolean isMember = userId != null &&
            groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);

        return GroupResponse.from(group, isMember);
    }

    /**
     * 그룹 멤버 목록 조회
     */
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupMembers(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        // 비공개 그룹은 멤버만 조회 가능
        if (!group.getIsPublic() &&
            !groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED,
                "비공개 그룹의 멤버 목록은 멤버만 조회할 수 있습니다.");
        }

        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);

        return members.stream()
            .map(GroupMemberResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * 그룹 포스트 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<GroupPostResponse> getGroupPosts(Long groupId, Long userId, Pageable pageable) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        // 비공개 그룹은 멤버만 조회 가능
        if (!group.getIsPublic() &&
            !groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED,
                "비공개 그룹의 게시물은 멤버만 조회할 수 있습니다.");
        }

        // 그룹에 공유된 피드 조회
        Page<Feed> feeds = feedRepository.findBySharedGroupAndIsDeletedFalse(group, pageable);

        List<GroupPostResponse> posts = feeds.stream()
            .map(GroupPostResponse::from)
            .collect(Collectors.toList());

        return new PageImpl<>(posts, pageable, feeds.getTotalElements());
    }

    /**
     * 그룹에 운동 공유
     */
    @Transactional
    public GroupPostResponse shareToGroup(Long userId, ShareToGroupRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Group group = groupRepository.findById(request.getGroupId())
            .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        // 멤버인지 확인
        if (!groupMemberRepository.existsByGroupIdAndUserId(request.getGroupId(), userId)) {
            throw new BusinessException(ErrorCode.NOT_GROUP_MEMBER,
                "그룹 멤버만 공유할 수 있습니다.");
        }

        // 운동 세션 조회 (선택적)
        Workout workout = null;
        if (request.getWorkoutId() != null) {
            workout = workoutRepository.findById(request.getWorkoutId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKOUT_NOT_FOUND));

            // 본인 운동인지 확인
            if (!workout.getUser().getId().equals(userId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED,
                    "본인의 운동만 공유할 수 있습니다.");
            }
        }

        // 피드 생성 (그룹에 공유)
        Feed.CardStyle cardStyle = null;
        if (request.getCardStyle() != null) {
            try {
                cardStyle = Feed.CardStyle.valueOf(request.getCardStyle().toUpperCase());
            } catch (IllegalArgumentException e) {
                // 잘못된 카드 스타일인 경우 null로 처리
                cardStyle = null;
            }
        }

        Feed feed = Feed.builder()
            .user(user)
            .workout(workout)
            .content(request.getContent())
            .imageUrl(request.getImageUrl())
            .cardStyle(cardStyle)
            .sharedGroup(group)
            .build();

        Feed savedFeed = feedRepository.save(feed);

        // 기여도 점수 증가
        GroupMember member = groupMemberRepository.findByGroupAndUser(group, user)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_GROUP_MEMBER));
        member.increaseContribution(10);  // 공유 시 10점
        groupMemberRepository.save(member);

        log.info("User {} shared to group {}: feed {}",
                 userId, request.getGroupId(), savedFeed.getId());

        return GroupPostResponse.from(savedFeed);
    }

    /**
     * 공개 그룹 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<GroupResponse> getPublicGroups(String sortBy, Pageable pageable) {
        Page<Group> groups;

        if ("popular".equals(sortBy)) {
            groups = groupRepository.findByIsPublicTrueAndIsActiveTrueOrderByMemberCountDesc(pageable);
        } else {
            groups = groupRepository.findByIsPublicTrueAndIsActiveTrueOrderByCreatedAtDesc(pageable);
        }

        return groups.map(group -> GroupResponse.from(group, false));
    }

    /**
     * 그룹 검색
     */
    @Transactional(readOnly = true)
    public Page<GroupResponse> searchGroups(String keyword, Pageable pageable) {
        Page<Group> groups = groupRepository.searchByName(keyword, pageable);
        return groups.map(group -> GroupResponse.from(group, false));
    }

    /**
     * 유니크한 초대 코드 생성
     */
    private String generateUniqueInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();

        do {
            code.setLength(0);
            for (int i = 0; i < 6; i++) {
                code.append(chars.charAt((int) (Math.random() * chars.length())));
            }
        } while (groupRepository.existsByInviteCode(code.toString()));

        return code.toString();
    }
}