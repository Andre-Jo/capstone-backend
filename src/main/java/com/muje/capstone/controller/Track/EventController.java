package com.muje.capstone.controller.Track;

import com.muje.capstone.dto.Track.EventRequestDto;
import com.muje.capstone.service.Track.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/track/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<String> trackEvents(@RequestBody List<EventRequestDto> dtos) {
        try {
            eventService.saveEvents(dtos);
            return ResponseEntity.ok("이벤트 저장 완료");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("오류: " + e.getMessage());
        }
    }
}