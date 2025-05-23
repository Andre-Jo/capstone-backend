package com.muje.capstone.controller.User;

/*
import com.muje.capstone.dto.User.SingUp_In.CompanyVerificationRequest;
import com.muje.capstone.dto.User.SingUp_In.CompanyVerificationResponse;
import com.muje.capstone.service.User.CompanyVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/signup/verify/company")
public class CompanyVerificationController {

    private final CompanyVerificationService companyVerificationService;

    @PostMapping
    public ResponseEntity<CompanyVerificationResponse> verifyCompany(
            @RequestBody @Valid CompanyVerificationRequest request,
            Principal principal // 현재 로그인한 사용자 정보
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    CompanyVerificationResponse.fail("로그인이 필요합니다.")
            );
        }

        CompanyVerificationResponse response = companyVerificationService.verifyCompany(
                request, principal.getName() // 로그인된 사용자 이메일/ID 전달
        );

        return ResponseEntity.ok(response);
    }
}
*/
