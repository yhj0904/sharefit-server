package com.sharegym.sharegym_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ShareGym 백엔드 애플리케이션 메인 클래스
 */
@SpringBootApplication
@EnableJpaAuditing // JPA Auditing 활성화 (생성일, 수정일 자동 관리)
@EnableAsync // 비동기 처리 활성화 (알림 등)
@EnableScheduling // 스케줄링 활성화 (일일 통계 등)
public class SharegymServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SharegymServerApplication.class, args);
	}

}
