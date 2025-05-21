package com.muje.capstone.dto.Community.PostLike;

import com.muje.capstone.domain.Community.Discussion;
import com.muje.capstone.domain.Community.GraduateReview;
import com.muje.capstone.domain.Community.Post;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PostListItemResponse {
    private Long id;
    private String title;
    private String contentPreview; // 목록에서는 내용 일부만 보여줄 수 있습니다.
    private String postType; // ✅ "GRADUATE_REVIEW" 또는 "DISCUSSION"
    private LocalDateTime createdAt;
    private int viewCount;
    private int likeCount;
    private int commentCount; // 해당 게시글의 총 댓글 수
    private String discussionCategory; // "QUESTION_TO_STUDENT", "QUESTION_TO_GRADUATE", "QUESTION_TO_ALL" 등

    // Post 엔티티를 받아서 DTO로 변환하는 생성자
    public PostListItemResponse(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        // 내용 미리보기 처리 (예시: 100자까지 보여주고 그 이상이면 "..." 추가)
        this.contentPreview = post.getContent() != null && post.getContent().length() > 100 ?
                post.getContent().substring(0, 100) + "..." : post.getContent();
        this.createdAt = post.getCreatedAt();
        this.viewCount = post.getViewCount();
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount(); // Post 엔티티의 총 댓글 수 필드 사용

        // ✅ 게시글 실제 타입에 따라 postType 및 상세 분류 설정
        if (post instanceof GraduateReview) {
            this.postType = "GRADUATE_REVIEW";
            this.discussionCategory = null; // 취업 후기는 상세 분류 없음
            // 필요하다면 GraduateReview 관련 추가 필드 설정
            // this.isAnonymous = ((GraduateReview) post).getIsAnonymous();
        } else if (post instanceof Discussion) {
            this.postType = "DISCUSSION";
            Discussion discussion = (Discussion) post;
            // DiscussionCategory enum 값을 String으로 변환하여 저장
            this.discussionCategory = discussion.getDiscussionCategory() != null ?
                    discussion.getDiscussionCategory().name() : null;
        } else {
            // 다른 타입의 Post가 있다면 처리 (예: 기본 Post 타입 등)
            this.postType = "UNKNOWN";
            this.discussionCategory = null;
        }
    }
}
