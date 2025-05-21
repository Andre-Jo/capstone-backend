package com.muje.capstone.dto.Payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TossBillingKeyIssueResponse { // Response from /v1/billing/authorizations/issue
    private String mId;
    private String customerKey;
    private String authenticatedAt;
    private String method;
    private String billingKey;
    // potentially card details, etc.
}