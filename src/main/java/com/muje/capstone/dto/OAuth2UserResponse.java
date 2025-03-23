package com.muje.capstone.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserResponse {
    private String email;
    private String nickname;
    private String profileImage;
}