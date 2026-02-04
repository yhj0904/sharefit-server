package com.sharegym.sharegym_server.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증 클래스
 */
@Slf4j
@Component
public class JwtProvider {

    private final Key key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") long accessTokenExpiration,
        @Value("${jwt.refresh-expiration}") long refreshTokenExpiration
    ) {
        // In development, use plain string; in production, use base64
        byte[] keyBytes;
        try {
            // Try to decode as base64 first
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (Exception e) {
            // If not base64, use plain string
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * Access Token 생성
     */
    public String generateAccessToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateToken(userPrincipal.getEmail(), userPrincipal.getId(), accessTokenExpiration);
    }

    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateToken(userPrincipal.getEmail(), userPrincipal.getId(), refreshTokenExpiration);
    }

    /**
     * 사용자 Email과 ID로 Access Token 생성
     */
    public String generateAccessToken(String email, Long userId) {
        return generateToken(email, userId, accessTokenExpiration);
    }

    /**
     * 사용자 Email과 ID로 Refresh Token 생성
     */
    public String generateRefreshToken(String email, Long userId) {
        return generateToken(email, userId, refreshTokenExpiration);
    }

    /**
     * 토큰 생성 내부 메서드
     * JWT에 이메일을 subject로, userId를 claim으로 저장
     */
    private String generateToken(String email, Long userId, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
            .setSubject(email)  // 이메일을 subject로 사용
            .claim("userId", userId)  // userId를 별도 claim으로 저장
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * 토큰에서 사용자 이메일 추출
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();

        return claims.getSubject();  // 이메일은 subject에 저장됨
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();

        return claims.get("userId", Long.class);  // userId는 claim에서 추출
    }

    /**
     * 토큰 유효성 검증
     * 예외를 catch하지 않고 상위로 전파하여 구체적인 에러 처리 가능
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
            throw ex;  // 만료된 토큰 예외는 재전파
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
            throw ex;  // 잘못된 형식의 토큰 예외는 재전파
        } catch (SecurityException | UnsupportedJwtException ex) {
            log.error("Invalid JWT signature or unsupported token");
            throw new MalformedJwtException("Invalid token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
            return false;  // 빈 토큰은 false 반환
        }
    }

    /**
     * 토큰 만료 시간 확인
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();

        return claims.getExpiration();
    }
}