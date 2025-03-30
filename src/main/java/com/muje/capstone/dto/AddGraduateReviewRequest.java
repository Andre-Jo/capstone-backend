package com.muje.capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddGraduateReviewRequest {
    private Boolean isAnonymous;
    private String title;
    private String content;
}