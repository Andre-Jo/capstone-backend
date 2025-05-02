package com.muje.capstone.controller;

import com.muje.capstone.dto.BillingKeyRequest;
import com.muje.capstone.dto.SubscriptionHistoryResponse;
import com.muje.capstone.dto.SubscriptionResponse;
import com.muje.capstone.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService svc;

    // 빌링키 등록 + 첫 구독
    @PostMapping("/register")
    public SubscriptionResponse register(
            Principal principal,
            @RequestBody BillingKeyRequest req
    ) {
        return svc.registerBillingKeyAndSubscribe(principal.getName(), req);
    }

    // 구독 취소 요청
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel(Principal principal) {
        svc.requestCancellation(principal.getName());
        return ResponseEntity.ok().build();
    }

    // 현재 구독 조회
    @GetMapping
    public SubscriptionResponse current(Principal principal) {
        return svc.getCurrent(principal.getName());
    }

    // 구독 이력 조회
    @GetMapping("/history")
    public List<SubscriptionHistoryResponse> history(Principal principal) {
        return svc.getHistory(principal.getName());
    }
}