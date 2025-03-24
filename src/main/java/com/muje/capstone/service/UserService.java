package com.muje.capstone.service;

import com.muje.capstone.domain.User;
import com.muje.capstone.domain.Graduate;
import com.muje.capstone.domain.Student;
import com.muje.capstone.dto.AddUserRequest;
import com.muje.capstone.dto.UserInfoResponse;
import com.muje.capstone.dto.UserType;
import com.muje.capstone.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public Long save(AddUserRequest dto) {

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + dto.getEmail());
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User user;

        if (dto.getUserType() == UserType.STUDENT) {
            user = Student.builder()
                    .email(dto.getEmail())
                    .password(encoder.encode(dto.getPassword()))
                    .nickname(dto.getNickname())
                    .school(dto.getSchool())
                    .department(dto.getDepartment())
                    .userType(dto.getUserType())
                    .profileImage(dto.getProfileImage())
                    .isSchoolVerified(dto.getIsSchoolVerified())
                    .studentYear(dto.getStudentYear())
                    .build();
        } else if (dto.getUserType() == UserType.GRADUATE) {
            user = Graduate.builder()
                    .email(dto.getEmail())
                    .password(encoder.encode(dto.getPassword()))
                    .nickname(dto.getNickname())
                    .school(dto.getSchool())
                    .department(dto.getDepartment())
                    .userType(dto.getUserType())
                    .profileImage(dto.getProfileImage())
                    .isSchoolVerified(dto.getIsSchoolVerified())
                    .currentCompany(dto.getCurrentCompany())
                    .currentSalary(dto.getCurrentSalary())
                    .skills(dto.getSkills())
                    .isCompanyVerified(dto.getIsCompanyVerified())
                    .build();
        } else {
            throw new IllegalArgumentException("지원하지 않는 사용자 유형입니다: " + dto.getUserType());
        }

        return userRepository.save(user).getId();
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email " + email + " not found"));
    }

    public UserInfoResponse getUserInfoByEmail(String email) {
        User user = findByEmail(email);

        // 공통 필드
        String userEmail = user.getEmail();
        String password = user.getPassword();
        String nickname = user.getNickname();
        String school = user.getSchool();
        String department = user.getDepartment();
        Integer studentYear = user.getStudentYear();
        UserType userType = user.getUserType();
        Integer points = user.getPoints();
        String profileImage = user.getProfileImage();
        LocalDateTime createdAt = user.getCreatedAt();
        LocalDateTime updatedAt = user.getUpdatedAt();
        Boolean isSchoolVerified = user.getIsSchoolVerified();

        // 재학생 전용 필드 (STUDENT 타입)
        Boolean isSubscribed = null;
        LocalDateTime subscriptionStartDate = null;
        LocalDateTime subscriptionEndDate = null;

        // 졸업생 전용 필드 (GRADUATE 타입)
        String currentCompany = null;
        String currentSalary = null;
        String skills = null;
        Boolean isCompanyVerified = null;

        if (user.getUserType() == UserType.STUDENT) {
            Student student = (Student) user;
            isSubscribed = student.getIsSubscribed();
            subscriptionStartDate = student.getSubscriptionStartDate();
            subscriptionEndDate = student.getSubscriptionEndDate();
        } else if (user.getUserType() == UserType.GRADUATE) {
            Graduate graduate = (Graduate) user;
            currentCompany = graduate.getCurrentCompany();
            currentSalary = graduate.getCurrentSalary();
            skills = graduate.getSkills();
            isCompanyVerified = graduate.getIsCompanyVerified();
        }

        return new UserInfoResponse(
                userEmail, password, nickname, school, department, studentYear, userType, points,
                profileImage, createdAt, updatedAt, isSchoolVerified,
                isSubscribed, subscriptionStartDate, subscriptionEndDate,
                currentCompany, currentSalary, skills, isCompanyVerified
        );
    }
}