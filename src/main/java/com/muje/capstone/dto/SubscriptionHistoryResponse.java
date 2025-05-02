package com.muje.capstone.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Builder
public class SubscriptionHistoryResponse {
    private Long historyId;
    private String orderId;
    private String paymentKey; // Only if successful
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // Customize format as needed
    private LocalDateTime periodStart;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // Customize format as needed
    private LocalDateTime periodEnd;

    private BigDecimal amount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // Customize format as needed
    private LocalDateTime requestedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") // Handles offset with timezone
    private OffsetDateTime approvedAt; // Only if successful
    private String failureMessage; // Only if failed
}