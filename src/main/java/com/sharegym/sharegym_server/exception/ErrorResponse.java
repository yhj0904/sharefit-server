package com.sharegym.sharegym_server.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 에러 응답 형식
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final boolean success;
    private final Object data;
    private final ErrorDetail error;
    private final LocalDateTime timestamp;

    /**
     * 에러 상세 정보
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetail {
        private final String code;
        private final String message;
        private final Object details;
    }

    /**
     * 에러 응답 생성 헬퍼 메서드
     */
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
            .success(false)
            .error(ErrorDetail.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .build())
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
            .success(false)
            .error(ErrorDetail.builder()
                .code(errorCode.name())
                .message(message)
                .build())
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, Object details) {
        return ErrorResponse.builder()
            .success(false)
            .error(ErrorDetail.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .details(details)
                .build())
            .timestamp(LocalDateTime.now())
            .build();
    }
}