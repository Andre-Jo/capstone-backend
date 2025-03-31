package com.muje.capstone.controller;

import com.muje.capstone.domain.Discussion;
import com.muje.capstone.dto.*;
import com.muje.capstone.service.DiscussionService;
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
@RequestMapping("/api/community/discussions")
public class DiscussionApiController {

    private final DiscussionService discussionService;
    private final UserService userService; // 로그인된 유저 정보를 조회하기 위한 서비스

    @PostMapping("/")
    public ResponseEntity<?> addDiscussion(@RequestBody AddDiscussionRequest request, Principal principal) {
        try {
            discussionService.save(request, principal.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body("게시글 작성 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<DiscussionResponse>> findAllDiscussion() {
        List<DiscussionResponse> responses = discussionService.findAll()
                .stream()
                .map(discussion -> {
                    UserInfoResponse writerInfo = userService.getUserInfoByEmail(discussion.getUser().getEmail());
                    return new DiscussionResponse(discussion, writerInfo);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscussionResponse> findDiscussion(@PathVariable long id) {
        Discussion discussion = discussionService.findById(id);
        UserInfoResponse userInfo = userService.getUserInfoByEmail(discussion.getUser().getEmail());
        return ResponseEntity.ok().body(new DiscussionResponse(discussion, userInfo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDiscussion(@PathVariable long id) {
        try {
            discussionService.delete(id);
            return ResponseEntity.ok().body("게시글 삭제 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDiscussion(@PathVariable long id,
                                                  @RequestBody UpdateDiscussionRequest request) {
        try {
            Discussion updatedDiscussion = discussionService.update(id, request);
            return ResponseEntity.ok().body("게시글 수정 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}