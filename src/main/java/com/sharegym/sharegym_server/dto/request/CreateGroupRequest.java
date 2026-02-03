package com.sharegym.sharegym_server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 그룹 생성 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequest {

    @NotBlank(message = "그룹 이름은 필수입니다.")
    @Size(min = 2, max = 100, message = "그룹 이름은 2-100자여야 합니다.")
    private String name;

    @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
    private String description;

    private String profileImageUrl;

    @Builder.Default
    private Boolean isPublic = false;  // 기본값: 비공개

    @Builder.Default
    private Integer maxMembers = 100;  // 기본값: 100명
}