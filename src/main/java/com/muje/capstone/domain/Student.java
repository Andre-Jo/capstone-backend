package com.muje.capstone.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Student extends User {

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", nullable = false)
    @Builder.Default
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.INACTIVE;

    @Column(name = "subscription_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal subscriptionFee;

    @Column(name = "subscription_start")
    private LocalDateTime subscriptionStart;

    @Column(name = "subscription_end")
    private LocalDateTime subscriptionEnd;

    @Column(name = "billing_key", unique = true)
    private String billingKey;

    @Column(name = "customer_key", unique = true, nullable = true) // 최초 생성 시 null일 수 있음
    private String customerKey;

    // activateSubscription, renewSubscription, requestCancellation, deactivateSubscription 등 메서드 (이전과 동일)
    public void activateSubscription(BigDecimal fee, String billingKey, String customerKey, LocalDateTime start, LocalDateTime end) {
        this.subscriptionStatus = SubscriptionStatus.ACTIVE;
        this.subscriptionFee = fee;
        this.billingKey = billingKey;
        this.customerKey = customerKey;
        this.subscriptionStart = start;
        this.subscriptionEnd = end;
    }

    public void renewSubscription(LocalDateTime newStart, LocalDateTime newEnd) {
        if (this.subscriptionStatus == SubscriptionStatus.ACTIVE) {
            this.subscriptionStart = newStart;
            this.subscriptionEnd = newEnd;
        } else {
            System.err.println("비활성 구독 갱신 시도: " + this.customerKey);
        }
    }

    public void requestCancellation() {
        this.subscriptionStatus = SubscriptionStatus.CANCELLATION_REQUESTED;
    }

    public void deactivateSubscription() {
        this.subscriptionStatus = SubscriptionStatus.INACTIVE;
    }

    public boolean isSubscriptionActive() {
        return this.subscriptionStatus == SubscriptionStatus.ACTIVE &&
                this.subscriptionEnd != null &&
                LocalDateTime.now().isBefore(this.subscriptionEnd);
    }

    public enum SubscriptionStatus {
        ACTIVE, INACTIVE, CANCELLATION_REQUESTED
    }
}