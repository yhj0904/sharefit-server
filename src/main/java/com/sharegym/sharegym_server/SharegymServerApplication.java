package com.sharegym.sharegym_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SharegymServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SharegymServerApplication.class, args);
	}

}
