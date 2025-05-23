package com.muje.capstone.dto.User;

import com.muje.capstone.domain.Notification.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private NotificationType type;     // 알림 타입 (CHAT, COMMENT, LIKE, SCRAP_EXPIRED 등)
    private String message;            // 알림 메시지
    private boolean read;              // 읽음 여부
    private LocalDateTime createdAt;   // 생성 시각

    private String roomId;             // CHAT 알림일 때
    private String senderNickname;     // CHAT 알림일 때
    private Long postId;               // COMMENT/LIKE 알림일 때
    private Long commentId;            // 댓글 좋아요 알림일 때
}