package com.muje.capstone.config.oauth;

import com.muje.capstone.config.jwt.TokenProvider;
import com.muje.capstone.domain.RefreshToken;
import com.muje.capstone.domain.User;
import com.muje.capstone.dto.OAuth2UserResponse;
import com.muje.capstone.repository.RefreshTokenRepository;
import com.muje.capstone.service.UserDetailService;
import com.muje.capstone.util.CookieUtil;
import com.muje.capstone.util.SubscriptionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.Duration;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${frontend.base-url}") // application.yml에서 값 주입
    private String frontendBaseUrl;

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    public static final String SOCIAL_USER_COOKIE_NAME = "socialUserInfo";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);
    // 신규 회원의 OAuth2User 정보를 임시로 보관할 쿠키 (예: 5분)
    public static final Duration SOCIAL_USER_COOKIE_DURATION = Duration.ofMinutes(5);

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDetailService userDetailService;
    private final SubscriptionUtil subscriptionUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // OAuth2User 정보 추출
        String email = (String) oAuth2User.getAttributes().get("email");
        String name = (String) oAuth2User.getAttributes().get("name");
        String picture = (String) oAuth2User.getAttributes().get("picture");

        User user = userDetailService.loadUserByUsername(email);

        if (user != null) {
            // 기존 회원이면 로그인 처리 (JWT 발급 등)
            handleExistingUserLogin(request, response, user);
            getRedirectStrategy().sendRedirect(request, response, frontendBaseUrl + "/");
        } else {
            // 신규 회원 - 회원가입 페이지로 이동
            OAuth2UserResponse userResponse = new OAuth2UserResponse(email, name, picture);
            String serializedUser = CookieUtil.serialize(userResponse);
            CookieUtil.addCookie(response, "socialUserInfo", serializedUser, 1800);
            getRedirectStrategy().sendRedirect(request, response, frontendBaseUrl + "/auth/register");
        }
    }

    // 기존 회원 로그인 처리: 토큰 발급 및 쿠키 저장, SecurityContext에 인증 객체 설정
    private void handleExistingUserLogin(HttpServletRequest request, HttpServletResponse response, User user) {
        // 액세스 토큰 발급 & 쿠키 저장
        String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
        CookieUtil.addCookie(response, ACCESS_TOKEN_COOKIE_NAME, accessToken, (int) ACCESS_TOKEN_DURATION.toSeconds());

        // 리프레시 토큰 발급, DB 저장 및 쿠키 저장
        String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
        saveRefreshToken(user.getId(), refreshToken);
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, (int) REFRESH_TOKEN_DURATION.toSeconds());

        // SecurityContext에 인증 객체 설정
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        subscriptionUtil.checkAndExpireSubscription(auth);
    }

    // 리프레시 토큰을 DB에 저장
    private void saveRefreshToken(Long userId, String newRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .map(entity -> entity.update(newRefreshToken))
                .orElse(new RefreshToken(userId, newRefreshToken));

        refreshTokenRepository.save(refreshToken);
    }
}