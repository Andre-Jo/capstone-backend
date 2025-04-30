package com.muje.capstone.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SubscriptionResponse {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private BigDecimal amount;
    private String transactionId;
}