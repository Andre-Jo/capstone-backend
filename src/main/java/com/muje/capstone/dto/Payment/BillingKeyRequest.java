package com.muje.capstone.dto.Payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillingKeyRequest {
    private String authKey;
    private String customerKey;
    private BigDecimal amount;
    private int period;
}