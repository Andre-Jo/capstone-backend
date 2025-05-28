package com.muje.capstone.dto.User.UserInfo;

import com.muje.capstone.domain.User.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SafeUserInfoResponse {
    private String email;
    private String nickname;
    private String school;
    private String department;
    private Integer studentYear; // 입학년도
    private User.UserType userType;   // STUDENT 또는 GRADUATE
    private String profileImage;
    private Boolean isSchoolVerified;

    // 재학생 필드
    private Boolean isSubscribed; // 구독 여부

    // 졸업생 추가 필드
    private String currentCompany;    // 현재 회사
    private String currentSalary;     // 현재 연봉
    private String occupation;        // 직업 분류
    private String skills;            // ex. Java, Python...
    private Boolean isCompanyVerified; // 회사 인증 여부

    public SafeUserInfoResponse(User user) {
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.school = user.getSchool();
        this.department = user.getDepartment();
        this.studentYear = user.getStudentYear();
        this.userType = user.getUserType();
        this.profileImage = user.getProfileImage();
        this.isSchoolVerified = user.getIsSchoolVerified();
    }
}