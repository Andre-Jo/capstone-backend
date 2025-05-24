package com.muje.capstone.dto.Track;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionStartRequest {
    private String userId;  // 프론트에서는 이메일을 userId로 보낸다고 가정
}