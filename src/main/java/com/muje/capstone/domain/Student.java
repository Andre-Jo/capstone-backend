package com.muje.capstone.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class Student extends User {

    @Column(name = "is_subscribed", nullable = false)
    private Boolean subscribed = false;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime subscriptionStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime subscriptionEnd;

    @Column(name = "billing_key")
    private String billingKey;

    @Column(name = "customer_uid")
    private String customerUid;

    public void activateSubscription(LocalDateTime start, LocalDateTime end) {
        this.subscribed = true;
        this.subscriptionStart = start;
        this.subscriptionEnd = end;
    }

    public void cancelSubscription() {
        this.subscribed = false;
        this.subscriptionStart = null;
        this.subscriptionEnd = null;
        this.billingKey = null;
        this.customerUid = null;
    }

    public void cancelSubscriptionButKeepActiveUntilExpiry() {
        this.billingKey = null;
        this.customerUid = null;
        // 구독 종료일(endDate)은 그대로 두고 isSubscribed는 true 유지
    }

}