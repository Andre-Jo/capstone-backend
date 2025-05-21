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
}
