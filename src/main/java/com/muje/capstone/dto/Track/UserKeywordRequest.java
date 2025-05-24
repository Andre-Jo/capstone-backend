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
    private String sessionId; // 수정: Long → String (DB와 일치)

    private String keywordType;
    private String keyword;
}