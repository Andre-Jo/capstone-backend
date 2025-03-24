package com.muje.capstone.controller;

import com.muje.capstone.dto.*;
import com.muje.capstone.service.AuthenticationService;
import com.muje.capstone.service.LogoutService;
import com.muje.capstone.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class UserApiController {

    private final UserService userService;
    private final LogoutService logoutService;
    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AddUserRequest request) {
        try {
            userService.save(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            LoginResponse loginResponse = authenticationService.login(request, response);
            return ResponseEntity.ok("로그인 성공");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("잘못된 이메일 또는 비밀번호");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        logoutService.logout(request, response);
        return ResponseEntity.ok("로그아웃 성공");
    }

    @GetMapping("/social-user")
    public ResponseEntity<OAuth2UserResponse> getSocialUserInfo(@AuthenticationPrincipal OAuth2User oAuth2User) {
        OAuth2UserResponse userResponse = userService.getSocialUserInfo(oAuth2User);

        if (userResponse == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }

        String email = authentication.getName();
        try {
            UserInfoResponse response = userService.getUserInfoByEmail(email);
            System.out.println(response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자 정보를 찾을 수 없습니다.");
        }
    }
}