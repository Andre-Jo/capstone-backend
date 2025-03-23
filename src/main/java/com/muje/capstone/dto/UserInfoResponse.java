package com.muje.capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfoResponse {
    private String email;
    private String nickname;
    private String school;
    private String department;
    private UserType userType; // STUDENT or GRADUATE
    private String profileImage;
    private Integer studentYear; // 입학년도
    private Boolean isSchoolVerified;

    // 졸업생 필드
    private String currentCompany; // 현재 회사
    private String currentSalary; // 현재 연봉 (숫자로 저장하는 게 나을 수도 있음)
    private String skills; // ex. Java, Python...
    private Boolean isCompanyVerified; // 기본값 false
}