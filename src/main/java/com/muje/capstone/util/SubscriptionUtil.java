package com.muje.capstone.util;

import com.muje.capstone.domain.Student;
import com.muje.capstone.domain.User;
import com.muje.capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SubscriptionUtil {

    private final UserRepository userRepository;

    public void checkAndExpireSubscription(Authentication authentication) {
        if (authentication == null) return;

        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            if (user instanceof Student student) {
                LocalDateTime endDate = student.getSubscriptionEndDate();
                if (Boolean.TRUE.equals(student.getIsSubscribed())
                        && endDate != null
                        && endDate.isBefore(LocalDateTime.now())) {

                    student.updateSubscriptionStatus(false, null, null); // 상태 비활성화
                    userRepository.save(student);
                }
            }
        }
    }
}