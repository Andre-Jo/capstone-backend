package com.muje.capstone.dto;

import lombok.Data;

@Data
public class CardRegisterRequest {
    private String authKey;
    private String customerKey;
}