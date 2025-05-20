package com.muje.capstone.util.Chat;

import com.muje.capstone.domain.Chat.Message;
import com.muje.capstone.dto.Chat.MessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageMapper {
    public MessageDto toDto(Message e) {
        return MessageDto.builder()
                .id(e.getId())
                .roomId(e.getRoomId())
                .senderId(e.getSenderId())
                .content(e.getContent())
                .createdAt(e.getCreatedAt())
                .build();
    }
}