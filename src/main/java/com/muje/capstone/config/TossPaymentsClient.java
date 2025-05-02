package com.muje.capstone.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muje.capstone.dto.TossBillingApprovalRequest;
import com.muje.capstone.dto.TossBillingKeyIssueResponse;
import com.muje.capstone.dto.TossPaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TossPaymentsClient {

    private final RestTemplate restTemplate;

    @Value("${tosspayments.url}") // e.g., https://api.tosspayments.com
    private String tossApiUrl;

    @Value("${tosspayments.secret-key}")
    private String secretKey; // Your Toss Payments Secret Key

    public TossPaymentsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * 빌링키 발급 요청 (Issue Billing Key)
     * POST /v1/billing/authorizations/issue
     */
    public TossBillingKeyIssueResponse issueBillingKey(String authKey, String customerKey) {
        String url = tossApiUrl + "/v1/billing/authorizations/issue";
        HttpHeaders headers = createHeaders();

        // Create request body
        BillingKeyIssueRequest requestBody = new BillingKeyIssueRequest(customerKey, authKey); // Simple DTO for request body

        HttpEntity<BillingKeyIssueRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<TossBillingKeyIssueResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    TossBillingKeyIssueResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            // Log detailed error including response body
            System.err.println("Error issuing billing key: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            // Consider throwing a custom exception
            throw new RuntimeException("Failed to issue billing key: " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            System.err.println("Error issuing billing key: " + e.getMessage());
            throw new RuntimeException("Failed to issue billing key.", e);
        }
    }

    /**
     * 빌링키로 결제 승인 (Approve Payment with Billing Key)
     * POST /v1/billing/{billingKey}
     */
    public TossPaymentResponse approveBillingPayment(String billingKey, TossBillingApprovalRequest request) {
        String url = tossApiUrl + "/v1/billing/" + billingKey;
        HttpHeaders headers = createHeaders();
        // Ensure idempotency key if needed/supported by this endpoint (check Toss Docs)
        // headers.set("Idempotency-Key", "...");

        HttpEntity<TossBillingApprovalRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<TossPaymentResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    TossPaymentResponse.class
            );
            // Basic check if response indicates success based on Toss API structure
            TossPaymentResponse paymentResponse = response.getBody();
            if (paymentResponse == null || !"DONE".equalsIgnoreCase(paymentResponse.getStatus())) { // Adjust based on actual success status values
                // Handle cases where API returns 200 OK but logical failure (e.g. payment failed)
                if (paymentResponse != null && paymentResponse.getFailure() != null) {
                    System.err.println("Billing payment failed: " + paymentResponse.getFailure().getCode() + " - " + paymentResponse.getFailure().getMessage());
                    // Return the response with failure details
                } else {
                    System.err.println("Billing payment failed with unexpected status: " + (paymentResponse != null ? paymentResponse.getStatus() : "null response"));
                    // You might still want to return the response object for logging/analysis
                }
            }
            return paymentResponse; // Return successful or failed payment details
        } catch (HttpClientErrorException e) {
            String body = e.getResponseBodyAsString();
            log.error("Error approving billing payment: {} - {}", e.getStatusCode(), body);
            // Try to parse TossPaymentResponse from body
            try {
                ObjectMapper mapper = new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                TossPaymentResponse errorResp = mapper.readValue(body, TossPaymentResponse.class);
                if (errorResp.getFailure() != null) {
                    return errorResp;
                }
            } catch (JsonProcessingException jsonEx) {
                log.warn("Could not parse error body into TossPaymentResponse: {}", jsonEx.getMessage());
            }
            throw new RuntimeException("Failed to approve billing payment: " + body, e);
        } catch (RestClientException e) {
            System.err.println("Error approving billing payment: " + e.getMessage());
            throw new RuntimeException("Failed to approve billing payment.", e);
        }
    }

/*
    */
    /**
     * 구독 해지 (Unsubscribe Billing Key)
     * DELETE /v1/billing/authorizations/{billingKey}
     *//*

    public void unsubscribe(String billingKey) {
        String url = tossApiUrl + "/v1/billing/authorizations/" + billingKey;
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }
*/


    // Helper DTO for the issue billing key request body
    @Data
    @AllArgsConstructor
    private static class BillingKeyIssueRequest {
        private String customerKey;
        private String authKey;
    }
}