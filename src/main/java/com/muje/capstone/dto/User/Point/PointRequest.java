package com.muje.capstone.dto.User.Point;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PointRequest {
    private int amount;              // 적립(+)/사용(-)된 포인트
    private String description;     // 내역 설명
}