package com.muje.capstone.domain.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gifticons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gifticon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl; // 실제 프론트에 전달할 이미지 URL 또는 코드
}