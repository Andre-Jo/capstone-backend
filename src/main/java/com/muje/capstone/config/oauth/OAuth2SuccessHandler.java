package com.muje.capstone.config.oauth;

import com.muje.capstone.config.jwt.TokenProvider;
import com.muje.capstone.domain.RefreshToken;
import com.muje.capstone.domain.User;
import com.muje.capstone.repository.RefreshTokenRepository;
import com.muje.capstone.repository.UserRepository;
import com.muje.capstone.util.CookieUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${frontend.base-url}") // application.yml에서 값 주입
    private String frontendBaseUrl;

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    public static final String OAUTH_USER_INFO_COOKIE_NAME = "oauth_user_info";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);
    public static final Duration OAUTH_USER_INFO_DURATION = Duration.ofMinutes(10);  // 신규 회원 정보 유지 시간

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");
        String nickname = (String) oAuth2User.getAttributes().get("name");
        String profileImage = (String) oAuth2User.getAttributes().get("picture");

        User user = findByEmail(email);

        if (user != null) {
            // 기존 회원이면 로그인 처리 후 home 이동
            handleExistingUserLogin(response, user);
            getRedirectStrategy().sendRedirect(request, response, frontendBaseUrl + "/");
        } else {
            // 신규 회원이면 OAuth 로그인 후 회원가입 페이지 이동
            getRedirectStrategy().sendRedirect(request, response, frontendBaseUrl + "/auth/register");
        }
    }

    public User findByEmail(String email) {
        try {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("User with email " + email + " not found"));
        } catch (EntityNotFoundException e) {
            return null; // 예외 발생 후 null 반환
        }
    }

    // 기존 회원 로그인 처리
    private void handleExistingUserLogin(HttpServletResponse response, User user) {
        // 액세스 토큰 발급 & 쿠키 저장
        String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
        CookieUtil.addCookie(response, ACCESS_TOKEN_COOKIE_NAME, accessToken, (int) ACCESS_TOKEN_DURATION.toSeconds());

        // 리프레시 토큰 발급 & 저장 & 쿠키 저장
        String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
        saveRefreshToken(user.getId(), refreshToken);
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, (int) REFRESH_TOKEN_DURATION.toSeconds());
    }

    // 리프레시 토큰을 DB에 저장
    private void saveRefreshToken(Long userId, String newRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .map(entity -> entity.update(newRefreshToken))
                .orElse(new RefreshToken(userId, newRefreshToken));

        refreshTokenRepository.save(refreshToken);
    }
}