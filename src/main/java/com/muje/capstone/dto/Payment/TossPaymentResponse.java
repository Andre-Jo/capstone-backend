package com.muje.capstone.dto.Payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TossPaymentResponse { // Common fields from Toss Payment Success/Failure responses
    private String version;
    private String paymentKey;
    private String type;
    private String orderId;
    private String orderName;
    private String mId;
    private String currency;
    private String method;
    private BigDecimal totalAmount;
    private BigDecimal balanceAmount;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") // ISO 8601 with timezone
    private OffsetDateTime requestedAt; // Parse from ISO 8601 String
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") // ISO 8601 with timezone
    private OffsetDateTime approvedAt;  // Parse from ISO 8601 String
    private TossFailure failure; // If payment failed
    @Data public static class TossFailure { private String code; private String message; }
}