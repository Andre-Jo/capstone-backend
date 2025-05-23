package com.muje.capstone.service;

import com.muje.capstone.domain.Chat.ChatRoom;
import com.muje.capstone.domain.Chat.Message;
import com.muje.capstone.domain.Notification.NotificationType;
import com.muje.capstone.domain.User.User;
import com.muje.capstone.dto.Chat.ChatRoomDto;
import com.muje.capstone.dto.Chat.ChatRoomView;
import com.muje.capstone.dto.Chat.MessageDto;
import com.muje.capstone.repository.Chat.ChatRoomRepository;
import com.muje.capstone.repository.Chat.MessageRepository;
import com.muje.capstone.repository.User.UserRepository;
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
    public ChatRoomDto findOrCreate1to1ChatRoom(String currentUserEmail, String otherUserEmail) {
        Long currentUserId = userRepository.findIdByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + currentUserEmail));
        Long otherUserId = userRepository.findIdByEmail(otherUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + otherUserEmail));

        String baseId = ChatRoom.generateBaseRoomId(currentUserId, otherUserId);

        ChatRoom room = chatRoomRepository.findById(baseId)
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

        // 상대 사용자 정보 조회
        Long otherId = room.getUser1Id().equals(currentUserId) ? room.getUser2Id() : room.getUser1Id();
        User other = userRepository.findById(otherId)
                .orElseThrow(() -> new IllegalArgumentException("상대 사용자를 찾을 수 없습니다: " + otherId));

        // DTO 반환
        return ChatRoomDto.builder()
                .roomId(room.getRoomId())
                .user1Id(room.getUser1Id())
                .user2Id(room.getUser2Id())
                .createdAt(room.getCreatedAt())
                .otherUserNickname(other.getNickname())
                .otherUserProfileImage(other.getProfileImage())
                .build();
    }

    public List<ChatRoomView> getUserChatRooms(Principal principal) {
        String email = principal.getName();
        Long userId = userRepository.findIdByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        return chatRoomRepository.findByUser1IdOrUser2Id(userId, userId).stream()
                .filter(room -> !room.getDeletedByUsers().contains(email))
                .map(room -> {
                    // 1) 내 ID와 반대되는 상대방 ID 계산
                    Long otherUserId = room.getUser1Id().equals(userId)
                            ? room.getUser2Id()
                            : room.getUser1Id();

                    // 2) 상대방 User 엔티티 조회
                    User other = userRepository.findById(otherUserId)
                            .orElseThrow(() -> new IllegalArgumentException("상대 사용자를 찾을 수 없습니다: " + otherUserId));

                    // 3) 마지막 메시지 조회
                    Optional<Message> lastMsg = messageRepository.findTopByRoomIdOrderByCreatedAtDesc(room.getRoomId());

                    // 4) ChatRoomView 생성
                    return new ChatRoomView(
                            room.getRoomId(),
                            room.getUser1Id(),
                            room.getUser2Id(),
                            room.getCreatedAt(),
                            lastMsg.map(Message::getContent).orElse(null),
                            lastMsg.map(Message::getCreatedAt).orElse(null),
                            other.getNickname(),
                            other.getProfileImage()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveAndBroadcastMessage(String roomId, Principal principal, String content) {
        User senderUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        Long receiverId;
        if (senderUser.getId().equals(room.getUser1Id())) {
            receiverId = room.getUser2Id();
        } else if (senderUser.getId().equals(room.getUser2Id())) {
            receiverId = room.getUser1Id();
        } else {
            throw new SecurityException("채팅방 참여자가 아닙니다.");
        }

        String broadcastTargetRoomId = roomId;

        Message chatMessage = Message.builder()
                .roomId(roomId)
                .senderId(senderUser.getId())
                .receiverId(receiverId)
                .content(content)
                .build();
        Message savedMessage = messageRepository.save(chatMessage);

        MessageDto messageDto = MessageDto.builder()
                .id(savedMessage.getId())
                .roomId(savedMessage.getRoomId())
                .senderId(savedMessage.getSenderId())
                .receiverId(savedMessage.getReceiverId())
                .content(savedMessage.getContent())
                .createdAt(savedMessage.getCreatedAt())
                .senderNickname(senderUser.getNickname())
                .senderProfileImage(senderUser.getProfileImage())
                .build();

        messagingTemplate.convertAndSend("/topic/chat/room/" + broadcastTargetRoomId, messageDto);

        String receiverEmail = userRepository.findEmailById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("수신자 이메일을 찾을 수 없습니다: " + receiverId));

        notificationService.createChatNotification(
                receiverEmail,
                senderUser.getNickname() + " – " + savedMessage.getContent(),
                roomId,
                senderUser.getNickname()
        );
    }

    public List<MessageDto> getMessagesByRoom(String roomId, Principal principal) {
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

        List<Message> messages = messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);

        // DTO로 매핑
        return messages.stream().map(msg -> {
            User sender = userRepository.findById(msg.getSenderId())
                    .orElseThrow(() -> new IllegalArgumentException("보낸 사용자를 찾을 수 없습니다: " + msg.getSenderId()));
            return MessageDto.builder()
                    .id(msg.getId())
                    .roomId(msg.getRoomId())
                    .senderId(msg.getSenderId())
                    .receiverId(msg.getReceiverId())
                    .content(msg.getContent())
                    .createdAt(msg.getCreatedAt())
                    .senderNickname(sender.getNickname())
                    .senderProfileImage(sender.getProfileImage())
                    .build();
        }).collect(Collectors.toList());
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