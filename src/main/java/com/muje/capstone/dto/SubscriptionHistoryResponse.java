package com.muje.capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SubscriptionHistoryResponse {
    private Long historyId;
    private String orderId;
    private String paymentKey;
    private String status;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private BigDecimal amount;
    private LocalDateTime requestedAt;
    private OffsetDateTime approvedAt;
    private String failureMessage;
}