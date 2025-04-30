package com.muje.capstone.dto;

import com.muje.capstone.domain.User;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class AddUserRequest {
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    private String email; // 학교 이메일 여부는 정책 논의 필요
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;
    @NotBlank(message = "학교명은 필수 입력 값입니다.")
    private String school;
    @NotBlank(message = "학과는 필수 입력 값입니다.")
    private String department;
    @NotNull(message = "사용자 유형은 필수 입력 값입니다.")
    private User.UserType userType; // STUDENT or GRADUATE
    private String profileImage;
    private Integer studentYear; // 입학년도 (Integer로 변경, null 허용)
    private Boolean isSchoolVerified = false;
    private Boolean isSocialLogin = false;

    // 졸업생만 입력하는 필드
    private String currentCompany; // 현재 회사
    private String currentSalary; // 현재 연봉 (숫자로 저장하는 게 나을 수도 있음)
    private String skills; // ex. Java, Python...
    private Boolean isCompanyVerified = false; // 기본값 false
}