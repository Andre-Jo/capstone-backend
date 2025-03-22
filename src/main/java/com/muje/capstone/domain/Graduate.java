package com.muje.capstone.domain;

import com.muje.capstone.dto.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Table(name = "graduate")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@SuperBuilder
public class Graduate extends User {

    @Column(name = "current_company")
    private String currentCompany; // 현재 회사

    @Column(name = "current_salary")
    private String currentSalary; // 현재 연봉

    @Lob
    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills; // 학습 스킬

    @Column(name = "is_company_verified")
    private Boolean isCompanyVerified; // 회사 인증 여부

    public Graduate(String email, String password, String nickname, String school, String department, int studentYear, UserType userType, String profileImage, Boolean isSchoolVerified, String currentCompany, String currentSalary, String skills, Boolean isCompanyVerified) {
        super(email, password, nickname, school, department, studentYear, userType, profileImage, isSchoolVerified);
        this.currentCompany = currentCompany;
        this.currentSalary = currentSalary;
        this.skills = skills;
        this.isCompanyVerified = isCompanyVerified;
    }
}
