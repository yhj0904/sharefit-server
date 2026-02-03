package com.sharegym.sharegym_server.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "prod"}) // 테스트 환경에서는 비활성화
public class S3Config {

    @Value("${aws.access-key:}")
    private String accessKey;

    @Value("${aws.secret-key:}")
    private String secretKey;

    @Value("${aws.region:ap-northeast-2}")
    private String region;

    @Bean
    public AmazonS3 amazonS3Client() {
        // 로컬 개발 환경에서는 Mock S3 클라이언트 반환 (선택사항)
        if (accessKey.isEmpty() || secretKey.isEmpty()) {
            return AmazonS3ClientBuilder.standard()
                    .withRegion(Regions.fromName(region))
                    .build();
        }

        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }
}