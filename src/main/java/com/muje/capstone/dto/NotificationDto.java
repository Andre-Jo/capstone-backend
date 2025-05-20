package com.muje.capstone.dto;

import com.muje.capstone.domain.Notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter @AllArgsConstructor
public class NotificationDto {
    private Long id;
    private NotificationType type;
    private String message;
    private String link;
    private boolean read;
    private LocalDateTime createdAt;
}