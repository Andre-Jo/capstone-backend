package com.muje.capstone.controller.User;

import com.muje.capstone.domain.Community.Post;
import com.muje.capstone.dto.Community.Comment.CommentResponse;
import com.muje.capstone.dto.Community.PostLike.PostListItemResponse;
import com.muje.capstone.dto.User.UserInfo.UserInfoResponse;
import com.muje.capstone.dto.User.UserInfo.UserUpdateRequest;
import com.muje.capstone.service.Community.CommentService;
import com.muje.capstone.service.Community.PostService;
import com.muje.capstone.service.User.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserManagementApiController {

    private final UserService userService;
    private final CommentService commentService;
    private final PostService postService;

    // 유저 정보 업데이트 API
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(
            Authentication authentication,
            @RequestBody UserUpdateRequest request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }

        String email = authentication.getName();
        try {
            // UserService의 updateUserInfo 메서드에서 UserType에 따라 내부적으로 처리
            UserInfoResponse response = userService.updateUserInfo(email, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }

        String email = authentication.getName();
        try {
            UserInfoResponse response = userService.getUserInfoByEmail(email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자 정보를 찾을 수 없습니다.");
        }
    }

    @GetMapping("/me/comments")
    public ResponseEntity<List<CommentResponse>> getMyComments(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userEmail = authentication.getName();
        List<CommentResponse> myComments = commentService.getCommentsByUser(userEmail);
        return ResponseEntity.ok(myComments);
    }

    @GetMapping("/me/posts")
    public ResponseEntity<List<PostListItemResponse>> getMyPosts(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userEmail = authentication.getName();
        List<Post> myPosts = postService.getPostsByUser(userEmail);

        // Post 엔티티 리스트를 PostListItemResponse DTO 리스트로 변환
        List<PostListItemResponse> responseList = myPosts.stream()
                .map(this::mapPostToResponseDto) // 아래에 DTO 매핑 메서드 사용
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    private PostListItemResponse mapPostToResponseDto(Post post) {
        // PostListItemResponse 생성자에서 Post 객체를 받아 변환 로직을 처리하도록 구현했으므로,
        // 단순히 새로운 DTO 객체를 생성하여 반환합니다.
        return new PostListItemResponse(post);
    }

    @DeleteMapping
    public ResponseEntity<?> deactivateCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }

        String email = authentication.getName();

        try {
            userService.deactivateUser(email);
            return ResponseEntity.ok().body("계정 사용 불가능 처리 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}