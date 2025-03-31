package com.muje.capstone.dto;

import com.muje.capstone.domain.Comment;
import com.muje.capstone.domain.Post;
import com.muje.capstone.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private PostDto post;
    private SafeUserInfoResponse user;
    private LocalDateTime createdAt;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.post = new PostDto(comment.getPost()); // PostDto 생성
        this.user = new SafeUserInfoResponse(comment.getUser()); // SafeUserInfoResponse 생성
        this.createdAt = comment.getCreatedAt();
    }

    @Getter
    public static class PostDto {
        private Long id;
        private String title;
        // 필요한 게시글 필드 추가

        public PostDto(Post post) {
            this.id = post.getId();
            this.title = post.getTitle();
        }
    }
}