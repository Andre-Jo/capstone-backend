package com.muje.capstone.config.oauth;

import com.muje.capstone.domain.User.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;
    private final boolean isNewUser;

    // 기존 회원인 경우
    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
        this.isNewUser = false;
    }

    // 신규 회원인 경우 (DB에 저장된 유저 정보는 없으므로 user는 null)
    public CustomOAuth2User(Map<String, Object> attributes) {
        this.user = null;
        this.attributes = attributes;
        this.isNewUser = true;
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 필요한 권한 정보를 반환 (필요시 구현)
        return null;
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}
