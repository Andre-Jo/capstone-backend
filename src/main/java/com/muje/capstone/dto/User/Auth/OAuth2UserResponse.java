package com.muje.capstone.dto.User.Auth;

import lombok.*;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OAuth2UserResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private String email;
    private String nickname;
    private String profileImage;
}