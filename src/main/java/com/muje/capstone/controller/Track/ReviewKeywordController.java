package com.muje.capstone.controller.Track;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.muje.capstone.dto.Track.ReviewKeywordDto;
import com.muje.capstone.service.Track.ReviewKeywordService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/track/review-keyword")
@RequiredArgsConstructor
public class ReviewKeywordController {

    private final ReviewKeywordService reviewKeywordService;

    @PostMapping("/keyword")
    public ResponseEntity<?> saveKeyword(@RequestBody ReviewKeywordDto dto) {
        try {
            reviewKeywordService.saveKeyword(dto);
            return ResponseEntity.status(201).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("키워드 저장 실패: " + e.getMessage());
        }
    }
}
