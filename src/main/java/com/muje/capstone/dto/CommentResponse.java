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
    private PostDto post;
    private CommentUserDto user;
    private String content;
    private LocalDateTime createdAt;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.post = new PostDto(comment.getPost());
        this.user = new CommentUserDto(comment.getUser());  // user 객체를 내부 DTO로 생성
        this.createdAt = comment.getCreatedAt();
    }

    @Getter
    public static class PostDto {
        private Long id;
        private String title;
        // 필요한 게시글 필드 추가 가능

        public PostDto(Post post) {
            this.id = post.getId();
            this.title = post.getTitle();
        }
    }

    @Getter
    public static class CommentUserDto {
        private String profileImage; // 프로필 이미지
        private String nickname;     // 닉네임
        private String school;       // 대학명
        private String department;   // 학과명

        public CommentUserDto(User user) {
            this.profileImage = user.getProfileImage();
            this.nickname = user.getNickname();
            this.school = user.getSchool();
            this.department = user.getDepartment();
        }
    }
}
