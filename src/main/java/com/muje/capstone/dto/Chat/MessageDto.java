package com.muje.capstone.dto.Chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private String roomId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private LocalDateTime createdAt;
}