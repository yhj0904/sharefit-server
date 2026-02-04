package com.sharegym.sharegym_server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharegym.sharegym_server.exception.ErrorCode;
import com.sharegym.sharegym_server.exception.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * JWT 인증 필터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // 토큰 검증과 처리를 별도로 수행
                if (!jwtProvider.validateToken(jwt)) {
                    // validateToken이 false를 반환한 경우 로그는 이미 JwtProvider에서 출력됨
                    // 여기서는 단순히 인증되지 않은 상태로 진행
                    filterChain.doFilter(request, response);
                    return;
                }

                // 토큰에서 이메일 추출 (일관성을 위해)
                String email = jwtProvider.getEmailFromToken(jwt);

                // 이메일로 사용자 조회
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException ex) {
            // 토큰 만료 - 명확한 에러 응답 반환
            log.error("JWT token is expired: {}", ex.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                "TOKEN_EXPIRED", "토큰이 만료되었습니다. 다시 로그인해 주세요.");
            return;
        } catch (MalformedJwtException | SignatureException ex) {
            // 잘못된 토큰 - 명확한 에러 응답 반환
            log.error("Invalid JWT token: {}", ex.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
            return;
        } catch (Exception ex) {
            // 기타 에러 - 로그만 남기고 계속 진행
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 에러 응답 전송
     */
    private void sendErrorResponse(HttpServletResponse response, int status,
                                  String errorCode, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.builder()
            .success(false)
            .error(ErrorResponse.ErrorDetail.builder()
                .code(errorCode)
                .message(message)
                .build())
            .timestamp(LocalDateTime.now())
            .build();

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    /**
     * Request Header에서 JWT 토큰 추출
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}