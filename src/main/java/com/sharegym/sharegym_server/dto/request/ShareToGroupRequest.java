package com.sharegym.sharegym_server.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 그룹에 게시물 공유 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareToGroupRequest {

    @NotNull(message = "그룹 ID는 필수입니다.")
    private Long groupId;

    private Long workoutId;  // 운동 세션 ID (선택적)

    private String content;  // 게시물 내용

    private String imageUrl;  // 이미지 URL (선택적)

    private String cardStyle;  // 카드 스타일 (선택적)

    // Frontend API에서 사용하는 추가 필드들
    private String workoutSnapshot;  // 운동 스냅샷 (JSON string)

    private String splitType;  // 카드 분할 타입 (2-split, 3-split 등)

    private Integer splitPosition;  // 분할 위치

    private String customOptions;  // 커스텀 옵션 (JSON string)
}