package com.muje.capstone.dto.User.UserInfo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequest {
    private String password;
    private String nickname;
    private String currentSalary; // 현재 연봉
    private String occupation; // 직업 분류
    private String skills; // 학습 스킬
}