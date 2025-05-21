package com.muje.capstone.controller.Chat;

import com.muje.capstone.domain.Chat.ChatRoom;
import com.muje.capstone.domain.Chat.Message;
import com.muje.capstone.dto.Chat.ChatRoomDto;
import com.muje.capstone.dto.Chat.ChatRoomView;
import com.muje.capstone.dto.Chat.MessageDto;
import com.muje.capstone.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 로그인한 사용자의 모든 채팅방 조회
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomView>> getUserChatRooms(Principal principal) {
        List<ChatRoomView> rooms = chatService.getUserChatRooms(principal);
        return ResponseEntity.ok(rooms);
    }

    // 두 사용자 간 채팅방 조회 또는 생성
    @GetMapping("/room")
    public ResponseEntity<ChatRoomDto> getOrCreateRoomForUsers(@RequestParam String targetEmail, Principal principal) {
        ChatRoomDto roomDto = chatService.findOrCreate1to1ChatRoom(principal.getName(), targetEmail);
        return ResponseEntity.ok(roomDto);
    }

    // 채팅방 내 메시지 조회
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<MessageDto>> getRoomMessages(@PathVariable String roomId, Principal principal) {
        List<MessageDto> messages = chatService.getMessagesByRoom(roomId, principal);
        return ResponseEntity.ok(messages);
    }

    // 채팅방 삭제
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> softDeleteRoom(@PathVariable String roomId, Principal principal) {
        chatService.softDeleteRoom(roomId, principal);
        return ResponseEntity.noContent().build();
    }

}