package com.muje.capstone.controller.Community;

import com.muje.capstone.domain.Community.GraduateReview;
import com.muje.capstone.dto.Community.GraduateReview.AddGraduateReviewRequest;
import com.muje.capstone.dto.Community.GraduateReview.GraduateReviewResponse;
import com.muje.capstone.dto.Community.GraduateReview.UpdateGraduateReviewRequest;
import com.muje.capstone.dto.User.UserInfo.UserInfoResponse;
import com.muje.capstone.service.Community.GraduateReviewService;
import com.muje.capstone.service.Community.ScrapService;
import com.muje.capstone.service.User.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/community/graduate-reviews")
public class GraduateReviewApiController {

    private final GraduateReviewService graduateReviewService;
    private final UserService userService;
    private final ScrapService scrapService;

    @PostMapping("/")
    public ResponseEntity<?> addGraduateReview(@RequestBody AddGraduateReviewRequest request, Principal principal) {
        if (!userService.isGraduate(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only graduates can write reviews.");
        }
        graduateReviewService.save(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body("취업 후기 작성 성공");
    }

    @GetMapping("/")
    public ResponseEntity<?> findAllGraduateReview(Principal principal) {
        /*if (!userService.canViewGraduateReviews(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근 권한이 없습니다.");
        }*/
        String email = principal.getName();
        Set<Long> myScraps = scrapService.findScrappedPostIds(email);

        List<GraduateReviewResponse> responses = graduateReviewService.findAll()
                .stream()
                .map(review -> {
                    UserInfoResponse writerInfo = userService.getUserInfoByEmail(review.getUser().getEmail());
                    GraduateReviewResponse dto = new GraduateReviewResponse(review, writerInfo);
                    dto.setScrapped(myScraps.contains(review.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findGraduateReview(@PathVariable long id, Principal principal) {
        /*if (!userService.canViewGraduateReviews(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근 권한이 없습니다.");
        }*/

        GraduateReview review = graduateReviewService.findById(id);
        UserInfoResponse userInfo = userService.getUserInfoByEmail(review.getUser().getEmail());

        GraduateReviewResponse dto = new GraduateReviewResponse(review, userInfo);
        dto.setScrapped(scrapService.isPostScrappedByUser(id));
        return ResponseEntity.ok(dto);
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