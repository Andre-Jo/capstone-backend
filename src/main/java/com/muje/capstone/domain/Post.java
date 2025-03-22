package com.muje.capstone.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Table(name = "Posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class) // Auditing 기능 활성화
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 설정 (필수)
    @JoinColumn(name = "user_id", nullable = false) // FK
    private User user; // 작성자 정보

    @Column(name = "is_anonymous")
    private Boolean isAnonymous; // 익명 여부

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content; // 취업 후기 상세 내용

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0; // 조회수

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0; // 좋아요 수

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Post(User user, Boolean isAnonymous, String title, String content) {
        this.user = user;
        this.isAnonymous = isAnonymous;
        this.title = title;
        this.content = content;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}