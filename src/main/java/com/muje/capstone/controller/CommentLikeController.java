package com.muje.capstone.controller;

import com.muje.capstone.dto.CommentLikeResponse;
import com.muje.capstone.dto.PostLikeResponse;
import com.muje.capstone.service.CommentLikeService;
import com.muje.capstone.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community/{postId}/comments/{commentId}/likes")
@RequiredArgsConstructor
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @PostMapping
    public ResponseEntity<Map<String, Boolean>> toggleLike(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Principal principal
    ) {
        boolean liked = commentLikeService.toggleLike(postId, commentId, principal.getName());
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    @GetMapping("/users")
    public ResponseEntity<List<CommentLikeResponse>> getLikedUsers(
            @PathVariable Long commentId
    ) {
        List<CommentLikeResponse> users = commentLikeService.getLikedUsers(commentId);
        return ResponseEntity.ok(users);
    }
}