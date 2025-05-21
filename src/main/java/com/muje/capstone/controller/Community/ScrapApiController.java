package com.muje.capstone.controller.Community;

import com.muje.capstone.dto.Community.ScrapResponse;
import com.muje.capstone.service.Community.ScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/community/scraps")
public class ScrapApiController {

    private final ScrapService scrapService;

    @PostMapping("/{postId}") // 변경: discussionId 대신 postId
    public ResponseEntity<Boolean> toggleScrap(@PathVariable Long postId) {
        boolean isScrapped = scrapService.toggleScrap(postId);
        return ResponseEntity.ok(isScrapped);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ScrapResponse>> getMyScraps() {
        List<ScrapResponse> myScraps = scrapService.getMyScraps();
        return ResponseEntity.ok(myScraps);
    }

    @GetMapping("/{postId}/check")
    public ResponseEntity<Boolean> checkScrapStatus(@PathVariable Long postId) {
        boolean isScrapped = scrapService.isPostScrappedByUser(postId);
        return ResponseEntity.ok(isScrapped);
    }
}