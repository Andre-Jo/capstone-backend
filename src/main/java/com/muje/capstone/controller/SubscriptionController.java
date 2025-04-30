package com.muje.capstone.controller;

import com.muje.capstone.dto.SubscriptionRequest;
import com.muje.capstone.dto.SubscriptionResponse;
import com.muje.capstone.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subService;

    @PostMapping("/sub")
    public ResponseEntity<SubscriptionResponse> subscribe(Principal principal,
                                                          @RequestBody SubscriptionRequest req) throws Exception {
        return ResponseEntity.ok(
                subService.subscribe(principal.getName(), req)
        );
    }

    @DeleteMapping
    public ResponseEntity<Void> cancel(Principal principal) {
        subService.cancel(principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<SubscriptionResponse> current(Principal principal) {
        SubscriptionResponse res = subService.current(principal.getName());
        if (res == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/history")
    public ResponseEntity<List<SubscriptionResponse>> history(Principal principal) {
        return ResponseEntity.ok(
                subService.history(principal.getName())
        );
    }
}
