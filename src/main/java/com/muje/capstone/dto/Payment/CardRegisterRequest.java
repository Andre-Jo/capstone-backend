package com.muje.capstone.dto.Payment;

import lombok.Data;

@Data
public class CardRegisterRequest {
    private String authKey;
    private String customerKey;
}