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
        String nickname = user.getNickname();
        String school = user.getSchool();
        String department = user.getDepartment();
        UserType userType = user.getUserType();
        String profileImage = user.getProfileImage();
        Integer studentYear = user.getStudentYear();
        Boolean isSchoolVerified = user.getIsSchoolVerified();

        // 졸업생 전용 필드 (Graduate인 경우)
        String currentCompany = null;
        String currentSalary = null;
        String skills = null;
        Boolean isCompanyVerified = false;

        if (user.getUserType() == UserType.GRADUATE) {
            Graduate graduate = (Graduate) user;
            currentCompany = graduate.getCurrentCompany();
            currentSalary = graduate.getCurrentSalary();
            skills = graduate.getSkills();
            isCompanyVerified = graduate.getIsCompanyVerified();
        }

        return new UserInfoResponse(
                userEmail,
                nickname,
                school,
                department,
                userType,
                profileImage,
                studentYear,
                isSchoolVerified,
                currentCompany,
                currentSalary,
                skills,
                isCompanyVerified
        );
    }

}