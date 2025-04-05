package com.muje.capstone.service;

import com.muje.capstone.domain.PointHistory;
import com.muje.capstone.domain.User;
import com.muje.capstone.dto.PointHistoryResponse;
import com.muje.capstone.dto.PointRequest;
import com.muje.capstone.repository.PointHistoryRepository;
import com.muje.capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;

    // 포인트 적립
    @Transactional
    public void accumulatePoints(String email, PointRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        int amount = request.getAmount();
        String description = request.getDescription();

        user.addPoints(amount);

        pointHistoryRepository.save(PointHistory.builder()
                .user(user)
                .amount(amount)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build());
    }

    // 포인트 차감
    @Transactional
    public void redeemPoints(String email, PointRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        int amount = request.getAmount();
        String description = request.getDescription();

        user.subtractPoints(amount);

        pointHistoryRepository.save(PointHistory.builder()
                .user(user)
                .amount(-amount) // 차감은 음수로 기록
                .description(description)
                .createdAt(LocalDateTime.now())
                .build());
    }

    // 포인트 내역 조회
    @Transactional(readOnly = true)
    public List<PointHistoryResponse> getPointHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return pointHistoryRepository.findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(PointHistoryResponse::fromEntity)
                .toList();
    }
}