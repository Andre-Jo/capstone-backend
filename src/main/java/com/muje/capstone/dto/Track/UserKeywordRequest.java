package com.muje.capstone.dto.Track;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserKeywordRequest {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("session_id")
    private Long sessionId;
    
    private String keywordType; // 필드명 (company, tech, etc)
    private String keyword;     // 저장할 키워드
}