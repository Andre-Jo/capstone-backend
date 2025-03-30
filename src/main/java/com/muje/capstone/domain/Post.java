package com.muje.capstone.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "Posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Inheritance(strategy = InheritanceType.JOINED) // 상속 받은 서브 클래스 id 선언 필요 없음
@SuperBuilder
@EntityListeners(AuditingEntityListener.class) // 👈 Auditing 기능 활성화
public abstract class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    protected String title;

    @Column(name = "content", nullable = false)
    protected String content;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    protected int viewCount = 0;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    protected int likeCount = 0;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    protected LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;

    public Post(String title, String content, int viewCount, int likeCount) {
        this.title = title;
        this.content = content;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
