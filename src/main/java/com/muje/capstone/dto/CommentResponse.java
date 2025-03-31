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
    private Post post;
    private User user;
    private LocalDateTime createdAt;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.post = comment.getPost();
        this.user = comment.getUser();
        this.createdAt = comment.getCreatedAt();
    }
}