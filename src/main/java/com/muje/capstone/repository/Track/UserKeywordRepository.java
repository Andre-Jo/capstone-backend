package com.muje.capstone.repository.Track;

import com.muje.capstone.domain.Track.UserKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {
    List<UserKeyword> findByUser_Email(String email);
    List<UserKeyword> findBySessionId(String sessionId);
}