package com.muje.capstone.controller;

import com.muje.capstone.dto.PointHistoryResponse;
import com.muje.capstone.dto.PointRequest;
import com.muje.capstone.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/points")
public class PointController {

    private final PointService pointService;

    // 포인트 적립
    @PostMapping("/accumulate")
    public ResponseEntity<?> accumulatePoints(@RequestBody PointRequest request, Principal principal) {
        pointService.accumulatePoints(principal.getName(), request);
        return ResponseEntity.ok("Points accumulated successfully.");
    }

    // 포인트 사용
    @PostMapping("/redeem")
    public ResponseEntity<?> redeemPoints(@RequestBody PointRequest request, Principal principal) {
        pointService.redeemPoints(principal.getName(), request);
        return ResponseEntity.ok("Points redeemed successfully.");
    }

    // 포인트 내역 조회
    @GetMapping("/history")
    public ResponseEntity<List<PointHistoryResponse>> getPointHistory(Principal principal) {
        List<PointHistoryResponse> history = pointService.getPointHistory(principal.getName());
        return ResponseEntity.ok(history);
    }
}
