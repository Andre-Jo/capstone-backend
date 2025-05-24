package com.muje.capstone.service.Track;

import com.muje.capstone.domain.Track.ReviewKeyword;
import com.muje.capstone.domain.User.User;
import com.muje.capstone.dto.Track.ReviewKeywordDto;
import com.muje.capstone.repository.Track.ReviewKeywordRepository;
import com.muje.capstone.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewKeywordService {

    private final ReviewKeywordRepository reviewKeywordRepository;
    private final UserRepository userRepository; // ✅ UserService 대신 UserRepository 직접 사용

    public void saveKeyword(ReviewKeywordDto dto) {
        if (dto.getUserId() == null && dto.getSessionId() == null) {
            throw new IllegalArgumentException("user_id 또는 session_id 중 하나는 반드시 있어야 합니다.");
        }

        User user = null;
        if (dto.getUserId() != null && !dto.getUserId().isBlank()) {
            user = userRepository.findByEmail(dto.getUserId()).orElse(null); // ✅ 직접 조회
        }

        ReviewKeyword keyword = ReviewKeyword.builder()
                .user(user)
                .sessionId(dto.getSessionId())
                .keywordType(dto.getKeywordType())
                .keyword(dto.getKeyword())
                .build();

        reviewKeywordRepository.save(keyword);
    }
}
