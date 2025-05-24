package com.muje.capstone.controller.Track;

import com.muje.capstone.dto.Track.SessionStartRequest;
import com.muje.capstone.dto.Track.SessionEndRequest;
import com.muje.capstone.dto.Track.SessionResponse;
import com.muje.capstone.service.Track.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/track/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/start")
    public ResponseEntity<SessionResponse> startSession(@RequestBody SessionStartRequest request) {
        SessionResponse session = sessionService.startSession(request.getUserId());
        return ResponseEntity.ok(session);
    }

    @PostMapping("/end")
    public ResponseEntity<?> endSession(@RequestBody SessionEndRequest request) {
        try {
            sessionService.endSession(request.getSessionId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
