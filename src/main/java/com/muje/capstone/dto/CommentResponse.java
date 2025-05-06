package com.muje.capstone.dto;

import com.muje.capstone.domain.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentResponse {
    private Long id;
    private PostDto post;
    private CommentUserDto user;
    private String content;
    private LocalDateTime createdAt;
    private List<CommentResponse> replies;

    // 최상위 댓글용 생성자
    public CommentResponse(Comment comment) {
        this.id        = comment.getId();
        this.post      = new PostDto(comment.getPost());
        this.user      = new CommentUserDto(comment.getUser());
        this.content   = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.replies   = new ArrayList<>();  // 서비스 계층에서 채워줄 것
    }

    @Getter
    public static class PostDto {
        private Long id;
        private String title;
        // 필요 시 더 추가

        public PostDto(Post post) {
            this.id    = post.getId();
            this.title = post.getTitle();
        }
    }

    @Getter
    public static class CommentUserDto {
        private String profileImage;
        private String nickname;

        // Student 전용
        private String school;
        private String department;

        // Graduate 전용
        private String company;
        private String occupation;

        public CommentUserDto(User user) {
            this.profileImage = user.getProfileImage();
            this.nickname     = user.getNickname();

            if (user instanceof Student student) {
                this.school     = student.getSchool();
                this.department = student.getDepartment();
            } else if (user instanceof Graduate graduate) {
                this.company    = graduate.getCurrentCompany();
                this.occupation = graduate.getOccupation();
            }
        }
    }
}