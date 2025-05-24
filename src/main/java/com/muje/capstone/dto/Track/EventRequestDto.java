package com.muje.capstone.dto.Track;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestDto {
    @JsonProperty("session_id")
    private Long session_id;

    @JsonProperty("user_id")
    private String userId;

    private String event_type;

    private String event_value;
    
    private String timestamp;
}