package com.muje.capstone.dto.Chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimpleMessagePayloadDTO {
    private String roomId;
    private String content;
}