package com.muje.capstone.dto.User.Point;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedeemResponse {
    private String status;      // WIN or LOSE
    private String gifticonUrl; // 당첨 시에만 값. 꽝이면 null
}