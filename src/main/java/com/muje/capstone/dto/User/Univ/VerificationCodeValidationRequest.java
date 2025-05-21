package com.muje.capstone.dto.User.Univ;

import lombok.Getter;

@Getter
public class VerificationCodeValidationRequest {
    private String univName;
    private String email;
    private int code;
}