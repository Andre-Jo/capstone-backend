package com.muje.capstone.dto;

import lombok.Getter;

@Getter
public class VerificationCodeValidationRequest {
    private String univName;
    private String email;
    private int code;
}