package com.muje.capstone.dto.Community.Comment;

import com.muje.capstone.domain.Community.Comment;
import com.muje.capstone.domain.Community.Post;
import com.muje.capstone.domain.User.Graduate;
import com.muje.capstone.domain.User.Student;
import com.muje.capstone.domain.User.User;
import com.muje.capstone.dto.User.UserInfo.UserInfoResponse;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private PostDto post;
    private CommentUserDto user;
    private String content;
    private LocalDateTime createdAt;
    private List<CommentResponse> replies;
    private Boolean isAdopted;

    // 최상위 댓글용 생성자
    public CommentResponse(Comment comment) {
        this.id        = comment.getId();
        this.post      = new PostDto(comment.getPost());
        this.user      = new CommentUserDto(comment.getUser());
        this.content   = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.replies   = new ArrayList<>();  // 서비스 계층에서 채워줄 것
        this.isAdopted = comment.getIsAdopted();
    }

    public CommentResponse(Comment comment, UserInfoResponse userInfo) {
        this.id = comment.getId();
        this.post = new PostDto(comment.getPost());
        this.user = new CommentUserDto(comment.getUser());
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.replies = new ArrayList<>(); // 채택 시에는 대댓글이 중요하지 않을 수 있지만, 구조상 유지
        this.isAdopted = comment.getIsAdopted();
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