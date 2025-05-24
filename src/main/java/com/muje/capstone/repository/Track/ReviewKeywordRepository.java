package com.muje.capstone.repository.Track;

import com.muje.capstone.domain.Track.ReviewKeyword;
import com.muje.capstone.domain.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewKeywordRepository extends JpaRepository<ReviewKeyword, Long> {

    List<ReviewKeyword> findTop7ByUserOrderByCreatedAtDesc(User user);
    List<ReviewKeyword> findTop7BySessionIdOrderByCreatedAtDesc(String sessionId);
    List<ReviewKeyword> findTop7ByUserAndKeywordTypeOrderByCreatedAtDesc(User user, String keywordType);
    List<ReviewKeyword> findTop7BySessionIdAndKeywordTypeOrderByCreatedAtDesc(String sessionId, String keywordType);
}
