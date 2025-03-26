package com.muje.capstone.domain;

import com.muje.capstone.dto.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Table(name = "student")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@SuperBuilder
public class Student extends User {

    // 구독 관련 정보
    @Column(name = "is_subscribed")
    private Boolean isSubscribed; // 구독 여부

    @Column(name = "subscription_start_date")
    private LocalDateTime subscriptionStartDate; // 구독 신청일

    @Column(name = "subscription_end_date")
    private LocalDateTime subscriptionEndDate; // 구독 만료일

    public Student(String email, String password, String nickname, String school, String department, int studentYear, UserType userType, String profileImage, Boolean isSchoolVerified, Boolean isSocialLogin, Boolean isSubscribed, LocalDateTime subscriptionStartDate, LocalDateTime subscriptionEndDate) {
        super(email, password, nickname, school, department, studentYear, userType, profileImage, isSchoolVerified, isSocialLogin);
        this.isSubscribed = isSubscribed;
        this.subscriptionStartDate = subscriptionStartDate;
        this.subscriptionEndDate = subscriptionEndDate;
    }
}
