package com.muje.capstone.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "subscription_histories")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SubscriptionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false) // student 테이블의 PK 참조
    private Student student;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "payment_key")
    private String paymentKey;

    @Column(name = "subscription_period_start", nullable = false)
    private LocalDateTime subscriptionPeriodStart;

    @Column(name = "subscription_period_end", nullable = false)
    private LocalDateTime subscriptionPeriodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "failure_code")
    private String failureCode;

    @Column(name = "failure_message", length = 500)
    private String failureMessage;

    public enum Status {
        PENDING, SUCCESS, FAILURE
    }
}
