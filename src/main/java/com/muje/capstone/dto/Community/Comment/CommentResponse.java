package com.muje.capstone.dto.Community.Comment;

import com.muje.capstone.domain.Community.Comment;
import com.muje.capstone.domain.Community.Post;
import com.muje.capstone.domain.User.Graduate;
import com.muje.capstone.domain.User.Student;
import com.muje.capstone.domain.User.User;
import com.muje.capstone.dto.User.UserInfo.UserInfoResponse;
import com.muje.capstone.repository.Community.CommentRepository;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private PostDto post;
    private CommentUserDto user;
    private String content;
    private LocalDateTime createdAt;
    private List<CommentResponse> replies = new ArrayList<>();
    private Boolean isAdopted;
    private int likeCount;

    public CommentResponse(Comment comment) {
        this.id        = comment.getId();
        this.post      = new PostDto(comment.getPost());
        this.user      = new CommentUserDto(comment.getUser());
        this.content   = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.isAdopted = comment.getIsAdopted();
        this.likeCount = comment.getLikeCount();
    }

    public CommentResponse(Comment comment, UserInfoResponse userInfo) {
        this.id = comment.getId();
        this.post = new PostDto(comment.getPost());
        this.user = new CommentUserDto(userInfo);
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.replies = new ArrayList<>(); // 채택 시에는 대댓글이 중요하지 않을 수 있지만, 구조상 유지
        this.isAdopted = comment.getIsAdopted();
        this.likeCount = comment.getLikeCount();
    }

    @Getter
    public static class PostDto {
        private Long id;
        private String title;

        public PostDto(Post post) {
            this.id    = post.getId();
            this.title = post.getTitle();
        }
    }

    @Getter
    public static class CommentUserDto {
        private String profileImage;
        private String nickname;
        private String school;
        private String department;
        private String company;
        private String occupation;

        public CommentUserDto(User user) {
            this.profileImage = user.getProfileImage();
            this.nickname = user.getNickname();
            this.school = user.getSchool();
            this.department = user.getDepartment();

            if (user instanceof Graduate graduate) {
                this.company    = graduate.getCurrentCompany();
                this.occupation = graduate.getOccupation();
            }
        }

        // UserInfoResponse 기반
        public CommentUserDto(UserInfoResponse info) {
            this.profileImage = info.getProfileImage();
            this.nickname     = info.getNickname();
            this.school       = info.getSchool();
            this.department   = info.getDepartment();
            this.company      = info.getCurrentCompany();
            this.occupation   = info.getOccupation();
        }
    }
}