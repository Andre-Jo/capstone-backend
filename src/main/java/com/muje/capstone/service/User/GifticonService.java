package com.muje.capstone.service.User;

import com.muje.capstone.domain.User.Gifticon;
import com.muje.capstone.domain.User.GifticonReward;
import com.muje.capstone.domain.User.GifticonReward.RewardStatus;
import com.muje.capstone.dto.User.Point.PointRequest;
import com.muje.capstone.dto.User.Point.RedeemResponse;
import com.muje.capstone.repository.User.GifticonRepository;
import com.muje.capstone.repository.User.GifticonRewardRepository;
import com.muje.capstone.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class GifticonService {

    private final UserRepository userRepository;
    private final GifticonRepository gifticonRepository;
    private final GifticonRewardRepository rewardRepository;
    private final PointService pointService;

    private final Random rnd = new Random();

    /**
     * 포인트 차감 후 10% 확률로 당첨된 기프티콘 하나를 리턴합니다.
     */
    @Transactional
    public RedeemResponse redeemGifticon(int points, String userEmail) {
        // 1) 포인트 차감
        pointService.redeemPoints(userEmail, new PointRequest(points, "기프티콘 교환"));

        // 2) 당첨 여부 계산 (0~99 중 <10 이면 당첨 = 10%)
        boolean win = rnd.nextInt(100) < 10;
        GifticonReward reward = GifticonReward.builder()
                .userId(resolveUserId(userEmail))
                .usedPoints(points)
                .status(win ? RewardStatus.WIN : RewardStatus.LOSE)
                .build();

        String url = null;
        if (win) {
            // 3) 당첨 시 무작위 하나 꺼내고
            Gifticon g = gifticonRepository.findRandomOne();
            reward.setGifticonId(g.getId());
            url = g.getImageUrl();
        }

        rewardRepository.save(reward);
        return new RedeemResponse(win ? "WIN" : "LOSE", url);
    }

    public List<GifticonReward> getUserRewards(String userEmail) {
        Long uid = resolveUserId(userEmail);
        return rewardRepository.findAllByUserIdOrderByCreatedAtDesc(uid);
    }

    private Long resolveUserId(String email) {
        return userRepository.findIdByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
    }
}