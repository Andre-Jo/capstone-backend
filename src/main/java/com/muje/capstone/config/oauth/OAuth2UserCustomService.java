
package com.muje.capstone.config.oauth;

import com.muje.capstone.domain.User;
import com.muje.capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
/*
@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // OAuth2 공급자로부터 유저 정보를 가져옴
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = (String) oAuth2User.getAttributes().get("email");

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            // 기존 회원: DB에 저장된 User를 사용하여 CustomOAuth2User 생성
            return new CustomOAuth2User(optionalUser.get(), oAuth2User.getAttributes());
        } else {
            // 신규 회원: OAuth2 공급자로 받은 정보를 그대로 담은 CustomOAuth2User 생성
            return new CustomOAuth2User(oAuth2User.getAttributes());
        }
    }
}
*/
