package com.muje.capstone.service.Track;

import com.muje.capstone.domain.Track.Session;
import com.muje.capstone.domain.User.User;
import com.muje.capstone.dto.Track.SessionResponse;
import com.muje.capstone.repository.Track.SessionRepository;
import com.muje.capstone.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public SessionResponse startSession(String email) {
        User user = null;
        if (email != null && !email.isBlank()) {
            user = userRepository.findByEmail(email)
                    .orElse(null); // 유저가 없을 경우 null로 설정
        }

        // ✅ 로그인 유저라면 미종료 세션 종료
        if (user != null) {
            sessionRepository.findTopByUserIdAndEndedAtIsNull(user.getId())
                    .ifPresent(existing -> {
                        existing.setEndedAt(LocalDateTime.now());
                        sessionRepository.save(existing);
                    });
        }

        Session session = Session.builder()
                .user(user) // 비로그인 유저는 null
                .startedAt(LocalDateTime.now())
                .build();

        Session savedSession = sessionRepository.save(session);

        return new SessionResponse(
                savedSession.getId(),
                user != null ? user.getEmail() : null,
                savedSession.getStartedAt(),
                savedSession.getEndedAt()
        );
    }

    public void endSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음: " + sessionId));

        session.setEndedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }
}
