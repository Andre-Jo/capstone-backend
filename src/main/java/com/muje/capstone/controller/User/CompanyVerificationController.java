package com.muje.capstone.controller.User;

import com.muje.capstone.dto.User.SingUp_In.CompanyVerificationCallbackRequest;
import com.muje.capstone.dto.User.SingUp_In.CompanyVerificationRequest;
import com.muje.capstone.dto.User.SingUp_In.CompanyVerificationResponse;
import com.muje.capstone.service.User.CompanyVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j // 로깅을 위해 Slf4j 어노테이션 추가
public class CompanyVerificationController {

    private final CompanyVerificationService companyVerificationService;

    @PostMapping
    public ResponseEntity<CompanyVerificationResponse> verifyCompany(
            @RequestBody @Valid CompanyVerificationRequest request
    ) {
        log.info("Received company verification request: birthDate={}, identity={}", request.getBirthDate(), request.getIdentity());

        CompanyVerificationResponse response = companyVerificationService.verifyCompany(request);

        if (response.isVerified()) {
            log.info("Company verification successful for user. Company: {}", response.getResCompanyNm());
            return ResponseEntity.ok(response);
        } else {
            log.warn("Company verification failed: Code={}, Message={}", response.getResultCode(), response.getResultMessage());
            return ResponseEntity.ok(response); // isVerified가 false로 설정되어 반환됨
        }
    }

    // 2차 추가 인증 콜백 요청 (새로 추가)
    @PostMapping("/callback")
    public ResponseEntity<CompanyVerificationResponse> verifyCompanyCallback(
            @RequestBody @Valid CompanyVerificationCallbackRequest callbackRequest
    ) {
        log.info("Received 2-Way verification callback request for transaction ID: {}", callbackRequest.getTransactionId());

        // 단일 객체로 변경된 request2WayResult 호출
        CompanyVerificationResponse response = companyVerificationService.request2WayResult(callbackRequest);

        if (response.isVerified()) {
            log.info("2-Way verification successful for transaction ID: {}", callbackRequest.getTransactionId());
            return ResponseEntity.ok(response);
        } else if (response.isContinue2Way()) {
            log.warn("2-Way verification is still pending or failed for transaction ID: {}. Code={}, Message={}",
                    callbackRequest.getTransactionId(), response.getResultCode(), response.getResultMessage());
            return ResponseEntity.accepted().body(response);
        } else {
            log.warn("2-Way verification failed for transaction ID: {}. Code={}, Message={}",
                    callbackRequest.getTransactionId(), response.getResultCode(), response.getResultMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}