package com.muje.capstone.service.Auth;


import com.muje.capstone.domain.RefreshToken;
import com.muje.capstone.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    // 리프레시 토큰 저장 또는 업데이트
    public void saveRefreshToken(Long userId, String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByUserId(userId)
                .map(entity -> entity.update(refreshToken))
                .orElse(new RefreshToken(userId, refreshToken));
        refreshTokenRepository.save(token);
    }

    // 리프레시 토큰으로 조회
    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected Token"));
    }

    // 리프레시 토큰 삭제
    public void deleteRefreshToken(Long userId) {
        refreshTokenRepository.findByUserId(userId)
                .ifPresent(refreshTokenRepository::delete);
    }
}
