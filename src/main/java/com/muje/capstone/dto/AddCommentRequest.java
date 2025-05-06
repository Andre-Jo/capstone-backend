package com.muje.capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
public class AddCommentRequest {
    private Long parentCommentId; // null이면 최상위 댓글
    private String content;
}