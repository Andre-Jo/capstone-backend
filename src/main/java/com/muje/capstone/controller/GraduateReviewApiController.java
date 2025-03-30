package com.muje.capstone.controller;

import com.muje.capstone.domain.GraduateReview;
import com.muje.capstone.dto.AddGraduateReviewRequest;
import com.muje.capstone.dto.GraduateReviewResponse;
import com.muje.capstone.dto.UpdateGraduateReviewRequest;
import com.muje.capstone.dto.UserInfoResponse;
import com.muje.capstone.service.GraduateReviewService;
import com.muje.capstone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/community/graduate-reviews")
public class GraduateReviewApiController {

    private final GraduateReviewService graduateReviewService;
    private final UserService userService; // 로그인된 유저 정보를 조회하기 위한 서비스

    @PostMapping("/")
    public ResponseEntity<?> addGraduateReview(@RequestBody AddGraduateReviewRequest request, Principal principal) {
        try {
            graduateReviewService.save(request, principal.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body("취업 후기 작성 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<GraduateReviewResponse>> findAllGraduateReview(Principal principal) {
        UserInfoResponse userInfo = userService.getUserInfoByEmail(principal.getName());
        List<GraduateReviewResponse> responses = graduateReviewService.findAll()
                .stream()
                .map(review -> new GraduateReviewResponse(review, userInfo))
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GraduateReviewResponse> findGraduateReview(@PathVariable long id) {
        GraduateReview review = graduateReviewService.findById(id);
        UserInfoResponse userInfo = userService.getUserInfoByEmail(review.getGraduate().getEmail());
        return ResponseEntity.ok().body(new GraduateReviewResponse(review, userInfo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGraduateReview(@PathVariable long id) {
        try {
            graduateReviewService.delete(id);
            return ResponseEntity.ok().body("취업 후기 삭제 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGraduateReview(@PathVariable long id,
                                                               @RequestBody UpdateGraduateReviewRequest request) {
        try {
            GraduateReview updatedGraduateReview = graduateReviewService.update(id, request);
            return ResponseEntity.ok().body("취업 후기 수정 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
