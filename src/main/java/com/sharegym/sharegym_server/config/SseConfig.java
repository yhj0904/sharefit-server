package com.sharegym.sharegym_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SSE (Server-Sent Events) 설정
 * 실시간 알림 및 이벤트 스트리밍을 위한 설정
 */
@Configuration
@EnableAsync
public class SseConfig implements WebMvcConfigurer {

    /**
     * 비동기 요청 타임아웃 설정
     * SSE 연결은 장시간 유지되므로 충분한 타임아웃 설정
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(60_000L); // 60초 타임아웃
    }
}