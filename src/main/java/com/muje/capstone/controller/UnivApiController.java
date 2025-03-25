package com.muje.capstone.controller;

import com.muje.capstone.dto.UnivNameRequest;
import com.muje.capstone.service.UnivService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/univ")
public class UnivApiController {

    private final UnivService univService;

    @PostMapping("/school-check")
    public ResponseEntity<?> checkUniversity(@RequestBody UnivNameRequest request) {
        String univName = request.getUnivName();
        boolean isValid = univService.isUniversityValid(univName);

        if (isValid) {
            return ResponseEntity.ok().body(Map.of("success", true));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "유효하지 않은 대학명입니다."));
        }
    }
}