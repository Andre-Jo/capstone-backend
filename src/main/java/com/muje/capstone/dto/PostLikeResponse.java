package com.muje.capstone.dto;

import com.muje.capstone.domain.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostLikeResponse {
    private Long id;
    private String email;
    private String nickname;
    private String school;
    private String department;
    private UserType userType;   // STUDENT 또는 GRADUATE
    private String profileImage;
}
