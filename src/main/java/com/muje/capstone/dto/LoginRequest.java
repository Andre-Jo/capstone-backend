package com.muje.capstone.dto;

import lombok.Getter;

@Getter
public class LoginRequest {
    private String email;
    private String password;
}