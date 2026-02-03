package com.sharegym.sharegym_server.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.sharegym.sharegym_server.exception.BusinessException;
import com.sharegym.sharegym_server.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"dev", "prod"})
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket:}")
    private String bucket;

    @Value("${aws.s3.base-url:}")
    private String baseUrl;

    /**
     * 파일 업로드
     * @param file 업로드할 파일
     * @param directory 저장할 디렉토리 (예: profile, feed, workout)
     * @return 업로드된 파일의 URL
     */
    public String uploadFile(MultipartFile file, String directory) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "파일이 비어있습니다.");
        }

        String fileName = generateFileName(file, directory);

        try {
            // 메타데이터 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            // S3에 파일 업로드
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucket, fileName, file.getInputStream(), metadata);

            // 공개 읽기 권한 설정
            putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);

            amazonS3.putObject(putObjectRequest);

            // URL 반환
            return getFileUrl(fileName);

        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "파일 업로드에 실패했습니다.");
        }
    }

    /**
     * 파일 삭제
     * @param fileUrl 삭제할 파일의 URL
     */
    public void deleteFile(String fileUrl) {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);

            if (amazonS3.doesObjectExist(bucket, fileName)) {
                amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
                log.info("파일 삭제 완료: {}", fileName);
            }
        } catch (Exception e) {
            log.error("파일 삭제 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.FILE_DELETE_ERROR, "파일 삭제에 실패했습니다.");
        }
    }

    /**
     * 프리사인드 URL 생성 (임시 다운로드 URL)
     * @param fileName 파일명
     * @param expirationMinutes URL 유효 시간(분)
     * @return 프리사인드 URL
     */
    public String generatePresignedUrl(String fileName, int expirationMinutes) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime() + (expirationMinutes * 60 * 1000L);
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, fileName)
                        .withMethod(com.amazonaws.HttpMethod.GET)
                        .withExpiration(expiration);

        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    /**
     * 파일 존재 여부 확인
     * @param fileName 파일명
     * @return 존재 여부
     */
    public boolean doesFileExist(String fileName) {
        return amazonS3.doesObjectExist(bucket, fileName);
    }

    /**
     * 파일명 생성
     */
    private String generateFileName(MultipartFile file, String directory) {
        String originalFileName = file.getOriginalFilename();
        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String uuid = UUID.randomUUID().toString();
        return directory + "/" + uuid + extension;
    }

    /**
     * 파일 URL 생성
     */
    private String getFileUrl(String fileName) {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return baseUrl + "/" + fileName;
        }
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    /**
     * URL에서 파일명 추출
     */
    private String extractFileNameFromUrl(String fileUrl) {
        if (baseUrl != null && !baseUrl.isEmpty() && fileUrl.startsWith(baseUrl)) {
            return fileUrl.substring(baseUrl.length() + 1);
        }

        // S3 URL 형식에서 파일명 추출
        String[] parts = fileUrl.split("/");
        if (parts.length >= 2) {
            // 마지막 두 부분이 directory/filename 형식
            return parts[parts.length - 2] + "/" + parts[parts.length - 1];
        }

        return fileUrl;
    }

    /**
     * 파일 크기 검증
     */
    public void validateFileSize(MultipartFile file, long maxSizeInMB) {
        long maxSizeInBytes = maxSizeInMB * 1024 * 1024;
        if (file.getSize() > maxSizeInBytes) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEED,
                    String.format("파일 크기는 %dMB를 초과할 수 없습니다.", maxSizeInMB));
        }
    }

    /**
     * 파일 형식 검증
     */
    public void validateFileType(MultipartFile file, String... allowedTypes) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE, "파일 형식을 확인할 수 없습니다.");
        }

        for (String type : allowedTypes) {
            if (contentType.startsWith(type)) {
                return;
            }
        }

        throw new BusinessException(ErrorCode.INVALID_FILE_TYPE, "허용되지 않은 파일 형식입니다.");
    }
}