package com.muje.capstone.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class SubscriptionResponse {
    private Long studentId;
    private String customerKey;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    // Maybe add next billing date?
}