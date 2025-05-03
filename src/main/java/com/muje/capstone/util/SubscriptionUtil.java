package com.muje.capstone.util;

import com.muje.capstone.domain.Student;
import com.muje.capstone.domain.User;
import com.muje.capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SubscriptionUtil {

    private final UserRepository userRepository;

    /**
     * 인증된 사용자(Student)에 대해 만료일 체크 후, ACTIVE 상태이면서 만료된 구독은 INACTIVE로 전환합니다.
     */
    public void checkAndExpireSubscription(Authentication authentication) {
        if (authentication == null) return;

        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            if (user instanceof Student student) {
                LocalDateTime endDate = student.getSubscriptionEnd();
                if ((student.getSubscriptionStatus() == Student.SubscriptionStatus.CANCELLATION_REQUESTED)
                        && endDate != null
                        && endDate.isBefore(LocalDateTime.now())) {

                    // 상태 및 구독 날짜 초기화
                    student.deactivateSubscription();
                    student.setSubscriptionStart(null);
                    student.setSubscriptionEnd(null);

                    userRepository.save(student);

                    // 인증 객체 업데이트
                    Authentication newAuth = new UsernamePasswordAuthenticationToken(
                            student, null, student.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                }
            }
        }
    }

}