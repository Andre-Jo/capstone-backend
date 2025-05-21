package com.muje.capstone.service;

import com.muje.capstone.domain.Notification;
import com.muje.capstone.domain.Notification.NotificationType;
import com.muje.capstone.dto.User.NotificationDto;
import com.muje.capstone.repository.NotificationRepository;
import com.muje.capstone.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;
    private final SimpMessageSendingOperations template;
    private final UserRepository userRepository;

    private Long resolveUserId(String email) {
        return userRepository.findIdByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
    }

    public NotificationDto createAndSend(String userEmail, NotificationType type, String message, String link) {
        Long userId = resolveUserId(userEmail);
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .link(link)
                .read(false)
                .build();

        notification = repo.save(notification);

        NotificationDto dto = toDto(notification);

        // 웹소켓 전송
        template.convertAndSend("/topic/notifications/" + userId, dto);
        return dto;
    }

    public List<NotificationDto> findAll(String userEmail) {
        Long userId = resolveUserId(userEmail);
        return repo.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long notificationId, String userEmail) {
        Long userId = resolveUserId(userEmail);
        Notification notification = repo.findById(notificationId)
                .filter(n -> n.getUserId().equals(userId))
                .orElseThrow(() -> new AccessDeniedException("본인의 알림이 아닙니다."));

        notification.setRead(true);
    }

    private NotificationDto toDto(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getType(),
                n.getMessage(),
                n.getLink(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}