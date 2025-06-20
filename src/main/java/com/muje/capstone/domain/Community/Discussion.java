package com.muje.capstone.domain.Community;

import com.muje.capstone.domain.User.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Table(name = "Discussions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@SuperBuilder
public class Discussion extends Post {

    public enum DiscussionCategory {
        QUESTION_TO_STUDENT,   // 재학생에게 질문
        QUESTION_TO_GRADUATE,    // 졸업생에게 질문
        QUESTION_TO_ALL          // 모두에게 질문
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "discussion_category")
    private DiscussionCategory discussionCategory;

    @Column(name = "adopted_comment_id")
    private Long adoptedCommentId;

    public Discussion(User user, String title, String content, int viewCount, int likeCount, int commentCount, DiscussionCategory discussionCategory) {
        super(user, title, content, viewCount, likeCount, commentCount);
        this.discussionCategory = discussionCategory;
        this.user = user;
    }

    public void updateDiscussion(String title, String content, DiscussionCategory discussionCategory) {
        super.update(title, content);
        this.discussionCategory = discussionCategory;
    }

    public void adoptComment(Long commentId) {
        if (this.adoptedCommentId != null) {
            throw new IllegalStateException("이미 채택된 댓글이 있습니다.");
        }
        this.adoptedCommentId = commentId;
    }

    public void unadoptComment() {
        this.adoptedCommentId = null;
    }
}
