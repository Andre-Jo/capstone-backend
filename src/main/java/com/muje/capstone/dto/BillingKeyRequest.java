package com.muje.capstone.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillingKeyRequest {
    private String customerKey; // 프론트에서 받은 customerKey
    private String authKey;     // 프론트에서 받은 authKey
    private BigDecimal amount;
}