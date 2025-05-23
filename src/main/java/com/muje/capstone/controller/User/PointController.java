package com.muje.capstone.controller.User;

import com.muje.capstone.domain.User.Gifticon;
import com.muje.capstone.domain.User.GifticonReward;
import com.muje.capstone.dto.User.Point.PointHistoryResponse;
import com.muje.capstone.dto.User.Point.PointRequest;
import com.muje.capstone.dto.User.Point.RedeemResponse;
import com.muje.capstone.service.User.GifticonService;
import com.muje.capstone.service.User.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/users/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;
    private final GifticonService gifticonService;

    // 포인트 적립
    @PostMapping("/accumulate")
    public ResponseEntity<?> accumulatePoints(
            @RequestBody PointRequest request,
            Principal principal
    ) {
        pointService.accumulatePoints(principal.getName(), request);
        return ResponseEntity.ok("Points accumulated successfully.");
    }

    // 포인트 사용 → 기프티콘 교환(당첨/꽝)
    @PostMapping("/redeem/gifticon")
    public ResponseEntity<RedeemResponse> redeemGifticon(
            @RequestBody PointRequest request,
            Principal principal
    ) {
        RedeemResponse resp = gifticonService.redeemGifticon(
                request.getAmount(), principal.getName());
        return ResponseEntity.ok(resp);
    }

    // 교환 내역 조회
    @GetMapping("/redeem/gifticon/history")
    public ResponseEntity<List<GifticonReward>> getGifticonHistory(Principal principal) {
        return ResponseEntity.ok(
                gifticonService.getUserRewards(principal.getName())
        );
    }

    // 포인트 내역 조회
    @GetMapping("/history")
    public ResponseEntity<List<PointHistoryResponse>> getPointHistory(
            Principal principal
    ) {
        List<PointHistoryResponse> history = pointService.getPointHistory(principal.getName());
        return ResponseEntity.ok(history);
    }
}