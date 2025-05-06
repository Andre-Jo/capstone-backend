package com.muje.capstone.dto;

import com.muje.capstone.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentLikeResponse {
    private Long id;
    private String email;
    private String nickname;
    private String school;
    private String department;
    private User.UserType userType;   // STUDENT 또는 GRADUATE
    private String profileImage;
}