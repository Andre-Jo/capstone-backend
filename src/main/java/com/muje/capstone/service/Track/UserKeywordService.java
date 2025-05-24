package com.muje.capstone.service.Track;

import com.muje.capstone.domain.Track.UserKeyword;
import com.muje.capstone.domain.User.User;
import com.muje.capstone.dto.Track.UserKeywordRequest;
import com.muje.capstone.repository.Track.UserKeywordRepository;
import com.muje.capstone.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserKeywordService {

    private final UserKeywordRepository keywordRepository;
    private final UserRepository userRepository;

    public void saveUserKeyword(UserKeywordRequest dto) {
        if (dto.getUserId() == null && dto.getSessionId() == null) {
            throw new IllegalArgumentException("user_id 또는 session_id 중 하나는 반드시 있어야 합니다.");
        }

        // 키워드 타입 유효성 검사
        List<String> validTypes = List.of("company", "tech", "job", "location", "salary", "tag");
        if (!validTypes.contains(dto.getKeywordType())) {
            throw new IllegalArgumentException("유효하지 않은 keywordType: " + dto.getKeywordType());
        }

        User user = null;
        if (dto.getUserId() != null) {
            user = userRepository.findByEmail(dto.getUserId()).orElse(null);
        }

        UserKeyword keyword = UserKeyword.builder()
                .user(user)
                .sessionId(dto.getSessionId() != null ? String.valueOf(dto.getSessionId()) : null)
                .keywordType(dto.getKeywordType())
                .keyword(dto.getKeyword())
                .createdAt(LocalDateTime.now())
                .build();

        keywordRepository.save(keyword);
    }

    public List<String> getLatestKeywordCombo(String userId, String sessionId) {
        List<String> result = new ArrayList<>();
        List<String> targetTypes = List.of("location", "job", "tech");

        for (String type : targetTypes) {
            List<UserKeyword> latest = keywordRepository.findLatestByType(
                    userId, sessionId, type, PageRequest.of(0, 1));
            if (!latest.isEmpty()) {
                result.add(latest.get(0).getKeyword());
            }
        }

        return result;
    }

}
