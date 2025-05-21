package com.muje.capstone.controller.Community;

import com.muje.capstone.dto.Community.Comment.AddCommentRequest;
import com.muje.capstone.dto.Community.Comment.CommentResponse;
import com.muje.capstone.service.Community.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/community/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<?> addComment(@PathVariable Long postId,
                                              @RequestBody AddCommentRequest addCommentRequest,
                                              Principal principal) {
        try {
            commentService.addComment(postId, addCommentRequest, principal.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body("댓글 작성 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        List<CommentResponse> list = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
}