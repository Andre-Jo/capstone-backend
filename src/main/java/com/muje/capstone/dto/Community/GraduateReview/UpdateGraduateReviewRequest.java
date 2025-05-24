package com.muje.capstone.dto.Community.GraduateReview;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateGraduateReviewRequest {
    private String title;
    private String content;
    private Boolean isAnonymous;

    private int q1;
    private int q2;
    private int q3;
    private int q4;
    private int q5;
    private double averageScore;
    private String colorIcon;
}

