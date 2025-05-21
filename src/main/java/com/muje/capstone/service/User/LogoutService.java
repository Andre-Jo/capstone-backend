package com.muje.capstone.service.User;

import com.muje.capstone.service.Auth.RefreshTokenService;
import com.muje.capstone.util.CookieUtil;
import com.muje.capstone.config.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LogoutService {
    private final RefreshTokenService refreshTokenService;
    private final TokenProvider tokenProvider;

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 액세스 토큰 가져오기
        String accessToken = CookieUtil.getCookie(request, "accessToken")
                .orElse(null);

        // 유효한 토큰이면 RefreshToken 삭제
        if (accessToken != null) {
            Long userId = tokenProvider.getUserId(accessToken);
            refreshTokenService.deleteRefreshToken(userId);
        }

        // 쿠키 삭제
        CookieUtil.deleteCookie(request, response, "accessToken");
        CookieUtil.deleteCookie(request, response, "refreshToken");
        CookieUtil.deleteCookie(request, response, "socialUserInfo");

        // Spring Security 로그아웃 처리
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
    }
}