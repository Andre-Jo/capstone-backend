package com.muje.capstone.controller;

import com.muje.capstone.dto.*;
import com.muje.capstone.service.AuthenticationService;
import com.muje.capstone.service.LogoutService;
import com.muje.capstone.service.UserService;
import com.muje.capstone.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class UserApiController {

    private final UserService userService;
    private final LogoutService logoutService;
    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody AddUserRequest request) {
        try {
            userService.save(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/signup/email")
    public ResponseEntity<String> checkEmailDuplication(@RequestBody EmailCheckRequest request) {
        boolean exists = userService.emailExists(request.getEmail());
        if (exists) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("이미 사용 중인 이메일입니다.");
        } else {
            return ResponseEntity
                    .ok("사용 가능한 이메일입니다.");
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
    public ResponseEntity<OAuth2UserResponse> getSocialUserInfo(HttpServletRequest request) {
        Optional<String> serializedUser = CookieUtil.getCookie(request, "socialUserInfo");
        if (serializedUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            OAuth2UserResponse userResponse = CookieUtil.deserialize(serializedUser.get(), OAuth2UserResponse.class);
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            e.printStackTrace(); // 또는 로깅 프레임워크 사용
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}