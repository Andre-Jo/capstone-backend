package com.muje.capstone.dto.Community.Discussion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AdoptCommentRequest {
    private Long commentId; // 채택할 댓글의 ID
}