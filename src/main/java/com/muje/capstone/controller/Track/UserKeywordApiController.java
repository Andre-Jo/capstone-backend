package com.muje.capstone.controller.Track;

import com.muje.capstone.dto.Track.UserKeywordRequest;
import com.muje.capstone.service.Track.UserKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/track/keyword")
@RequiredArgsConstructor
public class UserKeywordApiController {

    private final UserKeywordService keywordService;

    @PostMapping
    public ResponseEntity<?> saveKeyword(@RequestBody UserKeywordRequest request) {
        keywordService.saveUserKeyword(request);
        return ResponseEntity.ok().body("{\"status\":\"keyword saved\"}");
    }

    @GetMapping("/latest-combo")
    public ResponseEntity<List<String>> getLatestCombo(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String sessionId) {
        List<String> keywords = keywordService.getLatestKeywordCombo(userId, sessionId);
        return ResponseEntity.ok(keywords);
    }
}