package com.muje.capstone.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Table(name = "CommunityPosts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@SuperBuilder
public class CommunityPost extends Post {

    public enum CommunityCategory {
        QUESTION_TO_STUDENT,   // 재학생에게 질문
        QUESTION_TO_GRADUATE,    // 졸업생에게 질문
        QUESTION_TO_ALL          // 모두에게 질문
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "community_category")
    private CommunityCategory communityCategory;

    @Column(name = "author", nullable = false)
    private String author;

    public CommunityPost(String title, String content, int viewCount, int likeCount, CommunityCategory communityCategory, String author) {
        super(title, content, viewCount, likeCount);
        this.communityCategory = communityCategory;
        this.author = author;
    }
}
