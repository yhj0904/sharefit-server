package com.sharegym.sharegym_server.dto.request;

import com.sharegym.sharegym_server.entity.Feed;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 피드 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFeedRequest {

    @NotBlank(message = "내용은 필수입니다.")
    @Size(max = 2000, message = "내용은 2000자 이내여야 합니다.")
    private String content;

    private Long workoutId; // 연결된 운동 세션 ID (선택적)

    private String imageUrl; // 운동 카드 이미지 URL

    private Feed.CardStyle cardStyle; // 운동 카드 스타일

    private Long groupId; // 공유할 그룹 ID (선택적)
}