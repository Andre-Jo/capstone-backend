package com.muje.capstone.controller;

import com.muje.capstone.dto.UniversityEmailVerificationRequest;
import com.muje.capstone.dto.UniversityValidationRequest;
import com.muje.capstone.dto.VerificationCodeValidationRequest;
import com.muje.capstone.service.UnivService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/univ")
public class UnivApiController {

    private final UnivService univService;

    @PostMapping("/university/validate")
    public ResponseEntity<?> checkUniversity(@RequestBody UniversityValidationRequest request) {
        boolean isValid = univService.isUniversityValid(request);

        if (isValid) {
            return ResponseEntity.ok().body(Map.of("success", true));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "유효하지 않은 대학명입니다."));
        }
    }

    @PostMapping("/university-email/verification")
    public ResponseEntity<?> checkUniversityEmail(@RequestBody UniversityEmailVerificationRequest request) {
        boolean isValid = univService.isUniversityEmailValid(request);

        if (isValid) {
            return ResponseEntity.ok().body(Map.of("success", true));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "유효하지 않은 이메일입니다."));
        }
    }

    @PostMapping("/verification-code/validate")
    public ResponseEntity<?> checkCertifyCode(@RequestBody VerificationCodeValidationRequest request) {
        boolean isValid = univService.isUniversityEmailCodeValid(request);

        if (isValid) {
            return ResponseEntity.ok().body(Map.of("success", true));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "유효하지 않은 코드입니다."));
        }
    }

    @DeleteMapping("/users")
    public ResponseEntity<?> deleteAllUsers() {
        boolean isValid = univService.deleteAllUsers();

        if (isValid) {
            return ResponseEntity.ok().body(Map.of("success", true));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "유효하지 않은 코드입니다."));
        }
    }
}