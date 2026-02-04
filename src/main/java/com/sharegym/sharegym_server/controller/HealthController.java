package com.sharegym.sharegym_server.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${DB_HOST:not-set}")
    private String dbHost;

    @Value("${S3_BUCKET_NAME:not-set}")
    private String s3BucketName;

    @Value("${jwt.secret:not-set}")
    private String jwtSecret;

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("profile", activeProfile);
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }

    @GetMapping("/env")
    public Map<String, String> checkEnvironment() {
        Map<String, String> env = new HashMap<>();
        env.put("activeProfile", activeProfile);
        env.put("dbHost", dbHost);
        env.put("s3BucketName", s3BucketName);
        env.put("jwtConfigured", !jwtSecret.equals("not-set") ? "YES" : "NO");
        return env;
    }
}