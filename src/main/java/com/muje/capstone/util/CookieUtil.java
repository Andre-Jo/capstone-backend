package com.muje.capstone.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.SerializationUtils;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public class CookieUtil {

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        boolean isProd = "prod".equals(System.getenv("SPRING_PROFILES_ACTIVE"));
        cookie.setSecure(isProd); // 프로덕션에서만 Secure
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "None"); // 크로스사이트에서도 쿠키 전송 허용
        response.addCookie(cookie);
    }

    // 쿠키의 이름을 입력받아 쿠키 삭제
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return;
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

    public static Optional<String> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();

        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    // 객체를 직렬화해 쿠키의 값으로 변환
    public static String serialize(Object obj) {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(obj));
    }

    // 쿠키를 역직렬화해 객체로 변환
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        return cls.cast(
                SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie.getValue()))
        );
    }

    public static <T> T deserialize(String cookieValue, Class<T> cls) {
        try {
            byte[] data = Base64.getUrlDecoder().decode(cookieValue);
            Object deserializedObject = SerializationUtils.deserialize(data);
            if (cls.isInstance(deserializedObject)) {
                return cls.cast(deserializedObject);
            } else {
                throw new IllegalArgumentException("Failed to cast to the desired class: " + cls.getName() +
                        ", 실제 클래스: " + deserializedObject.getClass().getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Deserialization failed", e);
        }
    }
}
