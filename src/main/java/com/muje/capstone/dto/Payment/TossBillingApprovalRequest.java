package com.muje.capstone.dto.Payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TossBillingApprovalRequest { // Request Body for /v1/billing/{billingKey}
    private BigDecimal amount;
    private String customerKey;
    private String orderId;
    private String customerEmail; // Optional but good
    private String customerName; // Optional
    // Add other optional fields like taxFreeAmount if needed
}