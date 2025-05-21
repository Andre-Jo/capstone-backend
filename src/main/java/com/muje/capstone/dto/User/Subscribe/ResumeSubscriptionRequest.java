package com.muje.capstone.dto.User.Subscribe;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResumeSubscriptionRequest {
    private BigDecimal amount;
    private int period;
}
