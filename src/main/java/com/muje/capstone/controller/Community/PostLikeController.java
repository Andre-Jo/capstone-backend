package com.muje.capstone.controller.Community;

import com.muje.capstone.dto.Community.PostLike.PostLikeResponse;
import com.muje.capstone.service.Community.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community/{postId}/likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping
    public ResponseEntity<?> toggleLike(@PathVariable Long postId, Principal principal) {
        boolean liked = postLikeService.toggleLike(postId, principal.getName());
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    @GetMapping("/users")
    public ResponseEntity<List<PostLikeResponse>> getLikedUsers(@PathVariable Long postId) {
        List<PostLikeResponse> users = postLikeService.getLikedUsers(postId);
        return ResponseEntity.ok(users);
    }
}