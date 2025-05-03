package com.muje.capstone.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "subscription_histories")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder
public class SubscriptionHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "subscription_period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "subscription_period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "payment_key")
    private String paymentKey;

    @Column(name = "failure_code")
    private String failureCode;

    @Column(name = "failure_message", length = 500)
    private String failureMessage;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    public enum Status { PENDING, SUCCESS, FAILURE }
}

