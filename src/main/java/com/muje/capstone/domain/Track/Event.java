package com.muje.capstone.domain.Track;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.muje.capstone.domain.User.User;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // User 엔티티 참조 

    private String eventType;

    @Column(columnDefinition = "TEXT") // 🔧 JSON 문자열 저장을 위해 길이 확장
    private String eventValue;

    private LocalDateTime timestamp;
}
