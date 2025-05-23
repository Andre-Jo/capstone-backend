package com.muje.capstone.repository.User;

import com.muje.capstone.domain.User.GifticonReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GifticonRewardRepository extends JpaRepository<GifticonReward,Long> {
    List<GifticonReward> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}