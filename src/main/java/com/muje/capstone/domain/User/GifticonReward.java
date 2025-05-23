package com.muje.capstone.domain.User;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "gifticon_rewards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // 👈 Auditing 기능 활성화
public class GifticonReward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    // 사용한 포인트
    @Column(name = "used_points")
    private int usedPoints;

    // 당첨 여부: WIN or LOSE
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RewardStatus status;

    // 당첨 시 매핑되는 gifticon.id
    @Column(name = "gifticon_id")
    private Long gifticonId;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum RewardStatus {
        WIN, LOSE
    }
}
