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

    @PostMapping("/register")
    public SubscriptionResponse register(Principal p, @RequestBody BillingKeyRequest req) {
        return svc.registerBillingKeyAndSubscribe(p.getName(), req);
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel(Principal p) {
        svc.requestCancellation(p.getName());
        return ResponseEntity.ok().build();
    }

    // 재구독
    @PostMapping("/resume")
    public SubscriptionResponse resume(Principal p, @RequestBody BillingKeyRequest req) {
        return svc.resumeSubscription(p.getName(), req);
    }

    @GetMapping
    public SubscriptionResponse current(Principal p) {
        return svc.getCurrent(p.getName());
    }

    @GetMapping("/history")
    public List<SubscriptionHistoryResponse> history(Principal p) {
        return svc.getHistory(p.getName());
    }
}