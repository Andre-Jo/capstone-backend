package com.muje.capstone.repository.Track;

import com.muje.capstone.domain.Track.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findTopByUserIdAndEndedAtIsNull(Long userId); // ✅ 미종료 세션
}
