package com.sharegym.sharegym_server.controller;

import com.sharegym.sharegym_server.dto.response.ApiResponse;
import com.sharegym.sharegym_server.dto.response.FileUploadResponse;
import com.sharegym.sharegym_server.security.CurrentUser;
import com.sharegym.sharegym_server.security.UserPrincipal;
import com.sharegym.sharegym_server.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Profile({"dev", "prod"})
@Tag(name = "File", description = "파일 업로드 API")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final S3Service s3Service;

    @PostMapping(value = "/upload/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "프로필 이미지 업로드", description = "사용자 프로필 이미지를 업로드합니다.")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadProfileImage(
            @CurrentUser UserPrincipal currentUser,
            @Parameter(description = "프로필 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("프로필 이미지 업로드 요청 - userId: {}", currentUser.getId());

        // 파일 검증
        s3Service.validateFileType(file, "image/");
        s3Service.validateFileSize(file, 5); // 5MB 제한

        // S3에 업로드
        String fileUrl = s3Service.uploadFile(file, "profile");

        FileUploadResponse response = FileUploadResponse.builder()
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping(value = "/upload/feed", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "피드 이미지 업로드", description = "피드 게시물용 이미지를 업로드합니다. (최대 10개)")
    public ResponseEntity<ApiResponse<List<FileUploadResponse>>> uploadFeedImages(
            @CurrentUser UserPrincipal currentUser,
            @Parameter(description = "피드 이미지 파일들", required = true)
            @RequestParam("files") List<MultipartFile> files) {

        log.info("피드 이미지 업로드 요청 - userId: {}, 파일 개수: {}", currentUser.getId(), files.size());

        if (files.size() > 10) {
            throw new IllegalArgumentException("최대 10개의 이미지만 업로드할 수 있습니다.");
        }

        List<FileUploadResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            // 파일 검증
            s3Service.validateFileType(file, "image/");
            s3Service.validateFileSize(file, 10); // 10MB 제한

            // S3에 업로드
            String fileUrl = s3Service.uploadFile(file, "feed");

            FileUploadResponse response = FileUploadResponse.builder()
                    .fileUrl(fileUrl)
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .build();

            responses.add(response);
        }

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping(value = "/upload/workout", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "운동 이미지 업로드", description = "운동 세션 관련 이미지를 업로드합니다.")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadWorkoutImage(
            @CurrentUser UserPrincipal currentUser,
            @Parameter(description = "운동 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("운동 이미지 업로드 요청 - userId: {}", currentUser.getId());

        // 파일 검증
        s3Service.validateFileType(file, "image/");
        s3Service.validateFileSize(file, 10); // 10MB 제한

        // S3에 업로드
        String fileUrl = s3Service.uploadFile(file, "workout");

        FileUploadResponse response = FileUploadResponse.builder()
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping(value = "/upload/group", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "그룹 이미지 업로드", description = "그룹 프로필 이미지를 업로드합니다.")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadGroupImage(
            @CurrentUser UserPrincipal currentUser,
            @Parameter(description = "그룹 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("그룹 이미지 업로드 요청 - userId: {}", currentUser.getId());

        // 파일 검증
        s3Service.validateFileType(file, "image/");
        s3Service.validateFileSize(file, 5); // 5MB 제한

        // S3에 업로드
        String fileUrl = s3Service.uploadFile(file, "group");

        FileUploadResponse response = FileUploadResponse.builder()
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "파일 삭제", description = "업로드된 파일을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @CurrentUser UserPrincipal currentUser,
            @Parameter(description = "삭제할 파일 URL", required = true)
            @RequestParam("fileUrl") String fileUrl) {

        log.info("파일 삭제 요청 - userId: {}, fileUrl: {}", currentUser.getId(), fileUrl);

        // TODO: 파일 소유자 검증 로직 추가 필요
        s3Service.deleteFile(fileUrl);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/presigned-url")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "임시 다운로드 URL 생성", description = "파일의 임시 다운로드 URL을 생성합니다.")
    public ResponseEntity<ApiResponse<String>> generatePresignedUrl(
            @CurrentUser UserPrincipal currentUser,
            @Parameter(description = "파일명", required = true)
            @RequestParam("fileName") String fileName,
            @Parameter(description = "URL 유효 시간(분)", required = false)
            @RequestParam(value = "expiration", defaultValue = "60") int expirationMinutes) {

        log.info("임시 URL 생성 요청 - userId: {}, fileName: {}", currentUser.getId(), fileName);

        String presignedUrl = s3Service.generatePresignedUrl(fileName, expirationMinutes);

        return ResponseEntity.ok(ApiResponse.success(presignedUrl));
    }
}