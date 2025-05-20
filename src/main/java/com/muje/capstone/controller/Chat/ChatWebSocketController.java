package com.muje.capstone.controller.Chat;

import com.muje.capstone.dto.Chat.SimpleMessagePayloadDTO;
import com.muje.capstone.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;

    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(@Payload SimpleMessagePayloadDTO payload, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        if (payload.getRoomId() == null || payload.getContent() == null) {
            return;
        }
        chatService.saveAndBroadcastMessage(payload.getRoomId(), principal, payload.getContent());
    }
}