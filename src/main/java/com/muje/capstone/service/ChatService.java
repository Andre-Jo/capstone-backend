package com.muje.capstone.service;

import com.muje.capstone.domain.Chat.ChatRoom;
import com.muje.capstone.domain.Chat.Message;
import com.muje.capstone.domain.Notification.NotificationType;
import com.muje.capstone.domain.User;
import com.muje.capstone.dto.Chat.ChatRoomView;
import com.muje.capstone.dto.NotificationDto;
import com.muje.capstone.repository.Chat.ChatRoomRepository;
import com.muje.capstone.repository.Chat.MessageRepository;
import com.muje.capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public ChatRoom findOrCreate1to1ChatRoom(String currentUserEmail, String otherUserEmail) {
        Long currentUserId = userRepository.findIdByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + currentUserEmail));
        Long otherUserId = userRepository.findIdByEmail(otherUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + otherUserEmail));

        String baseId = ChatRoom.generateBaseRoomId(currentUserId, otherUserId);

        return chatRoomRepository.findById(baseId)
                .map(existingRoom -> {
                    if (existingRoom.getDeletedByUsers().remove(currentUserEmail)) {
                        chatRoomRepository.save(existingRoom);
                    }
                    return existingRoom;
                })
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.builder()
                            .roomId(baseId)
                            .user1Id(Math.min(currentUserId, otherUserId))
                            .user2Id(Math.max(currentUserId, otherUserId))
                            .build();
                    return chatRoomRepository.save(newRoom);
                });
    }

    public List<ChatRoomView> getUserChatRooms(Principal principal) {
        String email = principal.getName();
        Long userId = userRepository.findIdByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        return chatRoomRepository.findByUser1IdOrUser2Id(userId, userId).stream()
                .filter(room -> !room.getDeletedByUsers().contains(email))
                .map(room -> {
                    Optional<Message> lastMessage = messageRepository.findTopByRoomIdOrderByCreatedAtDesc(room.getRoomId());
                    return new ChatRoomView(
                            room.getRoomId(),
                            room.getUser1Id(),
                            room.getUser2Id(),
                            room.getCreatedAt(),
                            lastMessage.map(Message::getContent).orElse(null),
                            lastMessage.map(Message::getCreatedAt).orElse(null)
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveAndBroadcastMessage(String roomId, Principal principal, String content) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        Long receiverId;
        if (user.getId().equals(room.getUser1Id())) {
            receiverId = room.getUser2Id();
        } else if (user.getId().equals(room.getUser2Id())) {
            receiverId = room.getUser1Id();
        } else {
            throw new SecurityException("채팅방 참여자가 아닙니다.");
        }

        // 메시지 저장: baseRoomId 사용
        String baseRoomId = room.getRoomId();
        Message chatMessage = Message.builder()
                .roomId(baseRoomId)
                .senderId(user.getId())
                .receiverId(receiverId)
                .content(content)
                .build();
        Message savedMessage = messageRepository.save(chatMessage);

        // 채팅 메시지 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chat/room/" + baseRoomId, savedMessage);

        // 알림: receiver 이메일 조회 후 NotificationService 호출
        String receiverEmail = userRepository.findEmailById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("수신자 이메일을 찾을 수 없습니다: " + receiverId));
        notificationService.createAndSend(
                receiverEmail,
                NotificationType.CHAT,
                user.getNickname() + " – " + savedMessage.getContent(),
                "/chat/rooms/" + baseRoomId
        );
    }

    public List<Message> getMessagesByRoom(String roomId, Principal principal) {
        String email = principal.getName();
        Long userId = userRepository.findIdByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        if (!userId.equals(room.getUser1Id()) && !userId.equals(room.getUser2Id())) {
            throw new SecurityException("접근 거부: 이 채팅방의 참여자가 아닙니다.");
        }

        if (room.getDeletedByUsers().contains(email)) {
            return Collections.emptyList();
        }

        return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
    }

    @Transactional
    public void softDeleteRoom(String roomId, Principal principal) {
        String email = principal.getName();
        Long userId = userRepository.findIdByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        if (!userId.equals(room.getUser1Id()) && !userId.equals(room.getUser2Id())) {
            throw new SecurityException("삭제 권한 없음: 이 채팅방의 참여자가 아닙니다.");
        }

        boolean added = room.getDeletedByUsers().add(email);
        if (added) {
            chatRoomRepository.save(room);
        }

        String u1Email = userRepository.findEmailById(room.getUser1Id()).orElse(null);
        String u2Email = userRepository.findEmailById(room.getUser2Id()).orElse(null);

        if (u1Email != null && u2Email != null &&
                room.getDeletedByUsers().contains(u1Email) && room.getDeletedByUsers().contains(u2Email)) {
            messageRepository.deleteAllByRoomId(room.getRoomId());
            chatRoomRepository.delete(room);
        }
    }
}