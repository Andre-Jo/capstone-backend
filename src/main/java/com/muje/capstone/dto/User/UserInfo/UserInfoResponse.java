package com.muje.capstone.dto.User.UserInfo;

import com.muje.capstone.domain.User.Graduate;
import com.muje.capstone.domain.User.Student;
import com.muje.capstone.domain.User.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String email;
    private String nickname;
    private String school;
    private String department;
    private Integer studentYear; // 입학년도
    private User.UserType userType; // STUDENT or GRADUATE
    private Integer points;
    private String profileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isSchoolVerified;
    private Boolean isSocialLogin;
    private Boolean enabled;

    // 재학생 필드
    private Boolean isSubscribed; // 구독 여부
    private LocalDateTime subscriptionStartDate; // 구독 신청일
    private LocalDateTime subscriptionEndDate; // 구독 만료일

    // 졸업생 필드
    private String currentCompany; // 현재 회사
    private String currentSalary; // 현재 연봉 (숫자로 저장하는 게 나을 수도 있음)
    private String occupation; // 직업 분류
    private String skills; // ex. Java, Python...
    private Boolean isCompanyVerified; // 기본값 false

    public UserInfoResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.school = user.getSchool();
        this.department = user.getDepartment();
        this.studentYear = user.getStudentYear();
        this.userType = user.getUserType();
        this.points = user.getPoints();
        this.profileImage = user.getProfileImage();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.isSchoolVerified = user.getIsSchoolVerified();
        this.isSocialLogin = user.getIsSocialLogin();
        this.enabled = user.getEnabled();

        // 사용자 유형에 따라 추가 정보 설정
        if (user.getUserType() == User.UserType.STUDENT && user instanceof Student) {
            Student student = (Student) user;
            this.isSubscribed = student.isSubscriptionActive();
            this.subscriptionStartDate = student.getSubscriptionStart();
            this.subscriptionEndDate = student.getSubscriptionEnd();
        } else if (user.getUserType() == User.UserType.GRADUATE && user instanceof Graduate) {
            Graduate graduate = (Graduate) user;
            this.currentCompany = graduate.getCurrentCompany();
            this.currentSalary = graduate.getCurrentSalary();
            this.occupation = graduate.getOccupation();
            this.skills = graduate.getSkills();
            this.isCompanyVerified = graduate.getIsCompanyVerified();
        }
    }
}