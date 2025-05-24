package com.muje.capstone.dto.Track;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@AllArgsConstructor
public class SessionResponse {
    @JsonProperty("session_id")
    private Long sessionId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("started_at")
    private LocalDateTime startedAt;

    @JsonProperty("ended_at")
    private LocalDateTime endedAt;
}