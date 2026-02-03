package com.sharegym.sharegym_server.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Firebase Admin SDK 설정
 * FCM (Firebase Cloud Messaging) 푸시 알림을 위한 설정
 */
@Slf4j
@Configuration
@Profile({"dev", "prod"})
public class FirebaseConfig {

    @Value("${firebase.config-path:}")
    private String firebaseConfigPath;

    @Value("${firebase.project-id:}")
    private String projectId;

    /**
     * Firebase 초기화
     */
    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options;

                if (firebaseConfigPath != null && !firebaseConfigPath.isEmpty()) {
                    // 서비스 계정 키 파일 사용
                    GoogleCredentials credentials = GoogleCredentials
                            .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream());

                    options = FirebaseOptions.builder()
                            .setCredentials(credentials)
                            .build();

                    log.info("Firebase Admin SDK 초기화 완료 (서비스 계정 키 사용)");
                } else if (projectId != null && !projectId.isEmpty()) {
                    // 환경 변수나 기본 인증 사용 (GCP 환경)
                    options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .setProjectId(projectId)
                            .build();

                    log.info("Firebase Admin SDK 초기화 완료 (기본 인증 사용)");
                } else {
                    log.warn("Firebase 설정이 없습니다. FCM 기능이 비활성화됩니다.");
                    return;
                }

                FirebaseApp.initializeApp(options);
                log.info("Firebase 앱 초기화 성공");
            }
        } catch (IOException e) {
            log.error("Firebase Admin SDK 초기화 실패: {}", e.getMessage());
            // 개발 환경에서는 FCM 없이도 실행 가능하도록 예외를 던지지 않음
        } catch (Exception e) {
            log.error("Firebase 초기화 중 예상치 못한 오류: {}", e.getMessage());
        }
    }

    /**
     * FirebaseMessaging 빈 등록
     */
    @Bean
    public FirebaseMessaging firebaseMessaging() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseMessaging.getInstance();
            }
        } catch (Exception e) {
            log.warn("FirebaseMessaging 인스턴스 생성 실패: {}", e.getMessage());
        }
        return null; // FCM이 설정되지 않은 경우 null 반환
    }
}