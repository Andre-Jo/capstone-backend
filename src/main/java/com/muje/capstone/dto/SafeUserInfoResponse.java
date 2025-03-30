package com.muje.capstone.dto;

import com.muje.capstone.domain.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SafeUserInfoResponse {
    private String email;
    private String nickname;
    private String school;
    private String department;
    private Integer studentYear; // 입학년도
    private UserType userType;   // STUDENT 또는 GRADUATE
    private String profileImage;
    private Boolean isSchoolVerified;
    private Boolean enabled;

    // 재학생 필드
    private Boolean isSubscribed; // 구독 여부
    private LocalDateTime subscriptionStartDate; // 구독 신청일
    private LocalDateTime subscriptionEndDate; // 구독 만료일

    // 졸업생 추가 필드
    private String currentCompany;    // 현재 회사
    private String currentSalary;     // 현재 연봉
    private String skills;            // ex. Java, Python...
    private Boolean isCompanyVerified; // 회사 인증 여부
}