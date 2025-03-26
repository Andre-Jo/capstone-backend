package com.muje.capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserInfoResponse {
    private String email;
    private String password;
    private String nickname;
    private String school;
    private String department;
    private Integer studentYear; // 입학년도
    private UserType userType; // STUDENT or GRADUATE
    private Integer points;
    private String profileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isSchoolVerified;
    private Boolean isSocialLogin;

    // 재학생 필드
    private Boolean isSubscribed; // 구독 여부
    private LocalDateTime subscriptionStartDate; // 구독 신청일
    private LocalDateTime subscriptionEndDate; // 구독 만료일

    // 졸업생 필드
    private String currentCompany; // 현재 회사
    private String currentSalary; // 현재 연봉 (숫자로 저장하는 게 나을 수도 있음)
    private String skills; // ex. Java, Python...
    private Boolean isCompanyVerified; // 기본값 false
}