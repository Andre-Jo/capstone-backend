package com.muje.capstone.controller.Community;

import com.muje.capstone.domain.Community.GraduateReview;
import com.muje.capstone.dto.Community.GraduateReview.AddGraduateReviewRequest;
import com.muje.capstone.dto.Community.GraduateReview.GraduateReviewResponse;
import com.muje.capstone.dto.Community.GraduateReview.UpdateGraduateReviewRequest;
import com.muje.capstone.dto.User.UserInfo.UserInfoResponse;
import com.muje.capstone.service.Community.GraduateReviewService;
import com.muje.capstone.service.User.UserService;
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
    private final UserService userService;

    private int q1;
    private int q2;
    private int q3;
    private int q4;
    private int q5;
    private double averageScore;
    private String colorIcon;


    @PostMapping("/")
    public ResponseEntity<?> addGraduateReview(@RequestBody AddGraduateReviewRequest request, Principal principal) {
        if (!userService.isGraduate(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only graduates can write reviews.");
        }
        graduateReviewService.save(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body("취업 후기 작성 성공");
    }

    @GetMapping("/")
    public ResponseEntity<?> findAllGraduateReview() {

        List<GraduateReviewResponse> responses = graduateReviewService.findAll()
                .stream()
                .map(review -> {
                    UserInfoResponse writerInfo = userService.getUserInfoByEmail(review.getUser().getEmail());
                    return new GraduateReviewResponse(review, writerInfo);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findGraduateReview(@PathVariable long id) {

        GraduateReview review = graduateReviewService.findById(id);
        UserInfoResponse userInfo = userService.getUserInfoByEmail(review.getUser().getEmail());
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