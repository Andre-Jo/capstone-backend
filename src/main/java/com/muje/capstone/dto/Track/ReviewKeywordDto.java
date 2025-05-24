package com.muje.capstone.dto.Track;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewKeywordDto {

    @JsonProperty("user_id")
    private String userId; // nullable 허용 (비회원 사용 가능)

    @JsonProperty("session_id")
    private String sessionId; // nullable 허용

    @NotBlank(message = "키워드 타입은 필수입니다.")
    private String keywordType; // 예: school, skills 등

    @NotBlank(message = "키워드는 필수입니다.")
    private String keyword;
}
