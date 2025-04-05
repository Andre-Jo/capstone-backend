package com.muje.capstone.dto;

import com.muje.capstone.domain.PointHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointHistoryResponse {
    private int amount;              // 적립(+)/사용(-)된 포인트
    private String description;      // 내역 설명
    private LocalDateTime timestamp; // 발생 일시

    public static PointHistoryResponse fromEntity(PointHistory history) {
        return new PointHistoryResponse(
                history.getAmount(),
                history.getDescription(),
                history.getCreatedAt()
        );
    }
}
