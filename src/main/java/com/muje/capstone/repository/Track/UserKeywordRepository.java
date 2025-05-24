package com.muje.capstone.repository.Track;

import com.muje.capstone.domain.Track.UserKeyword;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {
    List<UserKeyword> findByUser_Email(String email);

    List<UserKeyword> findBySessionId(String sessionId);

    @Query("""
            SELECT uk FROM UserKeyword uk
            WHERE ((:userId IS NOT NULL AND uk.user.email = :userId)
               OR (:userId IS NULL AND uk.sessionId = :sessionId))
              AND uk.keywordType = :type
            ORDER BY uk.createdAt DESC
        """)
        
    List<UserKeyword> findLatestByType(
            @Param("userId") String userId,
            @Param("sessionId") String sessionId,
            @Param("type") String type,
            Pageable pageable);

    List<UserKeyword> findLatestByType(String userId, String sessionId, String type, PageRequest of);

}