package com.muje.capstone.dto;

import lombok.Getter;

@Getter
public class PointRequest {
    private int amount;              // 적립(+)/사용(-)된 포인트
    private String description;     // 내역 설명
}