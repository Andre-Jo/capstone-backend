package com.muje.capstone.controller.Community;

import com.muje.capstone.domain.Community.Comment;
import com.muje.capstone.domain.Community.Discussion;
import com.muje.capstone.dto.Community.Comment.CommentResponse;
import com.muje.capstone.dto.Community.Discussion.AddDiscussionRequest;
import com.muje.capstone.dto.Community.Discussion.AdoptCommentRequest;
import com.muje.capstone.dto.Community.Discussion.DiscussionResponse;
import com.muje.capstone.dto.Community.Discussion.UpdateDiscussionRequest;
import com.muje.capstone.dto.User.UserInfo.UserInfoResponse;
import com.muje.capstone.service.Community.CommentService;
import com.muje.capstone.service.Community.DiscussionService;
import com.muje.capstone.service.Community.ScrapService;
import com.muje.capstone.service.User.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/community/discussions")
public class DiscussionApiController {

    private final DiscussionService discussionService;
    private final UserService userService;
    private final CommentService commentService;
    private final ScrapService scrapService;

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
    public ResponseEntity<List<DiscussionResponse>> findAllDiscussion(Principal principal) {
        String email = principal.getName();
        // 사용자가 스크랩한 postId 목록
        Set<Long> myScraps = scrapService.findScrappedPostIds(email);

        List<DiscussionResponse> responses = discussionService.findAll()
                .stream()
                .map(discussion -> {
                    UserInfoResponse writerInfo = userService.getUserInfoByEmail(discussion.getUser().getEmail());
                    DiscussionResponse dto = new DiscussionResponse(discussion, writerInfo);
                    dto.setScrapped(myScraps.contains(discussion.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscussionResponse> findDiscussion(@PathVariable long id, Principal principal) {
        Discussion discussion = discussionService.findById(id); // 게시글 정보 조회
        UserInfoResponse userInfo = userService.getUserInfoByEmail(discussion.getUser().getEmail());

        CommentResponse adoptedCommentResponse = null;
        if (discussion.getAdoptedCommentId() != null) {
            Optional<Comment> adopted = commentService.getAdoptedCommentForDiscussion(id);
            if (adopted.isPresent()) {
                CommentResponse cr = new CommentResponse(adopted.get(), userService.getUserInfoByEmail(adopted.get().getUser().getEmail()));
                adoptedCommentResponse = cr;
            }
        }

        DiscussionResponse dto = new DiscussionResponse(discussion, userInfo, adoptedCommentResponse);
        // 상세 조회 시 스크랩 여부
        dto.setScrapped(scrapService.isPostScrappedByUser(id));
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDiscussion(@PathVariable long id) {
        try {
            discussionService.delete(id);
            return ResponseEntity.ok("게시글 삭제 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDiscussion(@PathVariable long id,
                                              @RequestBody UpdateDiscussionRequest request,
                                              Principal principal) {
        try {
            discussionService.update(id, request);
            return ResponseEntity.ok("게시글 수정 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{discussionId}/comments/adopt")
    public ResponseEntity<?> adoptComment(@PathVariable Long discussionId,
                                          @RequestBody AdoptCommentRequest request,
                                          Principal principal) {
        try {
            Comment adopted = commentService.adoptComment(discussionId, request.getCommentId(), principal.getName());
            UserInfoResponse adopterInfo = userService.getUserInfoByEmail(adopted.getUser().getEmail());
            CommentResponse response = new CommentResponse(adopted, adopterInfo);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}