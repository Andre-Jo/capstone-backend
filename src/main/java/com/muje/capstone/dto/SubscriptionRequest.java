package com.muje.capstone.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubscriptionRequest {
    @NotBlank
    private String impUid; // 아임포트 결제 고유 ID
}