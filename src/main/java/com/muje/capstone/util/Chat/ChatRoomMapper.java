package com.muje.capstone.util.Chat;

import com.muje.capstone.domain.Chat.ChatRoom;
import com.muje.capstone.dto.Chat.ChatRoomDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRoomMapper {
    public ChatRoomDto toDto(ChatRoom e) {
        return ChatRoomDto.builder()
                .roomId(e.getRoomId())
                .user1Id(e.getUser1Id())
                .user2Id(e.getUser2Id())
                .createdAt(e.getCreatedAt())
                .build();
    }
}