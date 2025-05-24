package com.muje.capstone.domain.Community;

import com.muje.capstone.domain.User.User;
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

    @Column
    private int q1;
    @Column
    private int q2;
    @Column
    private int q3;
    @Column
    private int q4;
    @Column
    private int q5;
    @Column
    private double averageScore;
    @Column
    private String colorIcon;

    @Column(name = "is_anonymous")
    @Builder.Default
    private Boolean isAnonymous = false;

    public GraduateReview(User user, String title, String content, int viewCount, int likeCount, int commentCount,
            Boolean isAnonymous) {
        super(user, title, content, viewCount, likeCount, commentCount);
        this.isAnonymous = isAnonymous;
    }

    public void updateReview(String title, String content, Boolean isAnonymous,
            int q1, int q2, int q3, int q4, int q5,
            double averageScore, String colorIcon) {
        super.update(title, content);
        this.isAnonymous = isAnonymous;
        this.q1 = q1;
        this.q2 = q2;
        this.q3 = q3;
        this.q4 = q4;
        this.q5 = q5;
        this.averageScore = averageScore;
        this.colorIcon = colorIcon;
    }

}