package com.muje.capstone.domain.Track;

import com.muje.capstone.domain.User.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // User 엔티티 참조 

    private String sessionId; // 비로그인 사용자 기준

    private String keywordType; // company, tech, job, location, salary, tag 등

    private String keyword;

    private LocalDateTime createdAt;
}