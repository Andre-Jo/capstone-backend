package com.muje.capstone.dto;

import com.muje.capstone.domain.Discussion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateDiscussionRequest {
    private Discussion.DiscussionCategory discussionCategory;
    private String title;
    private String content;
}