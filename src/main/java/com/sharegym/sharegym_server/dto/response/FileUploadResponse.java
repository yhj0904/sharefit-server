package com.sharegym.sharegym_server.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "파일 업로드 응답")
public class FileUploadResponse {

    @Schema(description = "업로드된 파일 URL", example = "https://s3.amazonaws.com/bucket/profile/uuid.jpg")
    private String fileUrl;

    @Schema(description = "원본 파일명", example = "profile.jpg")
    private String fileName;

    @Schema(description = "파일 크기 (bytes)", example = "1024000")
    private Long fileSize;

    @Schema(description = "파일 타입", example = "image/jpeg")
    private String contentType;
}