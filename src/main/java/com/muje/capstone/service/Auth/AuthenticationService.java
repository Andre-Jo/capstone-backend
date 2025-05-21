package com.muje.capstone.service.Auth;

import com.muje.capstone.config.jwt.TokenProvider;
import com.muje.capstone.domain.User.User;
import com.muje.capstone.dto.User.SingUp_In.LoginRequest;
import com.muje.capstone.dto.User.SingUp_In.LoginResponse;
import com.muje.capstone.repository.User.UserRepository;
import com.muje.capstone.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        // 이메일/비밀번호 검증
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        try {
            authenticationManager.authenticate(authToken);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("잘못된 이메일 또는 비밀번호입니다.");
        }

        // 사용자 정보 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // JWT 토큰 생성
        String accessToken = tokenProvider.generateToken(user, Duration.ofHours(2));
        String refreshToken = tokenProvider.generateToken(user, Duration.ofDays(7));

        // 리프레시 토큰 저장
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);

        // 쿠키에 토큰 저장 (HttpOnly, Secure 옵션 적용)
        CookieUtil.addCookie(response, "accessToken", accessToken, 2 * 60 * 60);
        CookieUtil.addCookie(response, "refreshToken", refreshToken, 7 * 24 * 60 * 60);

        return new LoginResponse(accessToken, refreshToken);
    }
}