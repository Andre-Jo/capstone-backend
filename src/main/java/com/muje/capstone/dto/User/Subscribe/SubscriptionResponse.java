package com.muje.capstone.dto.User.Subscribe;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor @NoArgsConstructor @Data
public class SubscriptionResponse {
    private Long studentId;
    private String customerKey;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}