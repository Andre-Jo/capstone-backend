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
    private final TokenService tokenService;
    private final static String ACCESS_TOKEN_COOKIE = "accessToken";
    private final static String REFRESH_TOKEN_COOKIE = "refreshToken";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // public 엔드포인트는 토큰 검증 로직을 건너뜁니다.
        if (isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 쿠키에서 accessToken, refreshToken 값을 추출
        String accessToken = getTokenFromCookie(request, ACCESS_TOKEN_COOKIE);
        String refreshToken = getTokenFromCookie(request, REFRESH_TOKEN_COOKIE);

        // 액세스 토큰 검증 및 인증 처리
        if (accessToken != null && tokenProvider.validToken(accessToken)) {
            Authentication authentication = tokenProvider.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else if (refreshToken != null && tokenProvider.validToken(refreshToken)) {
            try {
                String newAccessToken = tokenService.createNewAccessToken(refreshToken);
                CookieUtil.addCookie(response, ACCESS_TOKEN_COOKIE, newAccessToken, (int) Duration.ofHours(2).toSeconds());
                Authentication authentication = tokenProvider.getAuthentication(newAccessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is invalid or expired.");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api")) {
            return true;
        }
        if (uri.startsWith("/api/auth/") && !uri.equals("/api/auth/me")) {
            return true;
        }
        if (uri.startsWith("/oauth2") && uri.startsWith("/api/univ/")) {
            return true;
        }
        return false;
    }

    private String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}