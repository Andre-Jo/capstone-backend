package com.muje.capstone.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "GraduateReviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@SuperBuilder
public class GraduateReview extends Post {

    @Column(name = "is_anonymous")
    @Builder.Default
    private Boolean isAnonymous = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graduate_id")
    private Graduate graduate;

    public GraduateReview(String title, String content, int viewCount, int likeCount, Boolean isAnonymous, Graduate graduate) {
        super(title, content, viewCount, likeCount);
        this.isAnonymous = isAnonymous;
        this.graduate = graduate;
    }

    public void updateReview(String title, String content, Boolean isAnonymous) {
        super.update(title, content);
        this.isAnonymous = isAnonymous;
    }
}