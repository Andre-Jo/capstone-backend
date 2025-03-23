package com.muje.capstone.config;

import com.muje.capstone.config.jwt.TokenProvider;
import com.muje.capstone.service.TokenService;
import com.muje.capstone.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private final TokenService tokenService; // TokenService 추가
    private final static String ACCESS_TOKEN_COOKIE = "accessToken"; // accessToken 쿠키 이름
    private final static String REFRESH_TOKEN_COOKIE = "refreshToken"; // refreshToken 쿠키 이름

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 쿠키에서 accessToken, refreshToken 값을 추출
        String accessToken = getAccessTokenFromCookie(request);
        String refreshToken = getRefreshTokenFromCookie(request);

        // 엑세스 토큰이 유효하면 인증 정보를 설정
        if (accessToken != null && tokenProvider.validToken(accessToken)) {
            Authentication authentication = tokenProvider.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else if (refreshToken != null && tokenProvider.validToken(refreshToken)) {
            // 엑세스 토큰이 만료되었거나 유효하지 않으면 리프레시 토큰을 사용하여 엑세스 토큰 재발급
            try {
                String newAccessToken = tokenService.createNewAccessToken(refreshToken);
                // 새로운 엑세스 토큰을 쿠키에 저장
                CookieUtil.addCookie(response, ACCESS_TOKEN_COOKIE, newAccessToken, (int) Duration.ofHours(2).toSeconds());
                // 인증 정보를 설정
                Authentication authentication = tokenProvider.getAuthentication(newAccessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (IllegalArgumentException e) {
                // 리프레시 토큰도 유효하지 않으면 재로그인 유도
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is invalid or expired.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // 쿠키에서 특정 이름의 토큰 값을 추출하는 메서드
    private String getAccessTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
