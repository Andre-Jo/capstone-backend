package com.muje.capstone.domain.Track;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.muje.capstone.domain.User.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 내부 고유 세션 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // User 엔티티 참조 

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt; //세션 시작 시각

    @Column(name = "ended_at")
    private LocalDateTime endedAt; // 세션 종료 시각
}
