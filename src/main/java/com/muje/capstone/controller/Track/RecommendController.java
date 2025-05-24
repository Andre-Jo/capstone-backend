package com.muje.capstone.controller.Track;

import com.muje.capstone.dto.Track.JobPostingResponse;
import com.muje.capstone.service.Track.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping("/refresh")
    public ResponseEntity<List<JobPostingResponse>> getRecommendedJobs(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String sessionId
    ) {
        List<JobPostingResponse> jobs = recommendService.getRecommendedJobs(userId, sessionId);
        return ResponseEntity.ok(jobs);
    }
}
