package com.muje.capstone.service;


import com.muje.capstone.domain.RefreshToken;
import com.muje.capstone.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(Long userId, String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByUserId(userId)
                .map(entity -> entity.update(refreshToken))
                .orElse(new RefreshToken(userId, refreshToken));
        refreshTokenRepository.save(token);
    }

    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected Token"));
    }
}
