package com.muje.capstone.service;

import com.muje.capstone.config.TossPaymentsClient;
import com.muje.capstone.domain.Student;
import com.muje.capstone.domain.SubscriptionHistory;
import com.muje.capstone.domain.User;
import com.muje.capstone.dto.*;
import com.muje.capstone.repository.StudentRepository;
import com.muje.capstone.repository.SubscriptionHistoryRepository;
import com.muje.capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SubscriptionHistoryRepository historyRepository;
    private final TossPaymentsClient tossPaymentsClient;
    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private Student findStudentByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        if (!(user instanceof Student)) {
            throw new RuntimeException("Not a student: " + email);
        }
        return (Student) user;
    }

    @Transactional
    public SubscriptionResponse registerBillingKeyAndSubscribe(String email, BillingKeyRequest req) {
        Student s = findStudentByEmail(email);
        if (s.getSubscriptionStatus() == Student.SubscriptionStatus.ACTIVE) {
            throw new RuntimeException("Already subscribed");
        }
        // 1) issue billing key
        TossBillingKeyIssueResponse issue = tossPaymentsClient.issueBillingKey(req.getAuthKey(), req.getCustomerKey());
        BigDecimal fee = req.getAmount() != null ? req.getAmount() : s.getSubscriptionFee();
        // 2) first payment
        triggerRenewalPayment(s, issue.getBillingKey(), issue.getCustomerKey(), fee, true);
        // 3) activate subscription
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusMonths(1);
        s.activateSubscription(fee, issue.getBillingKey(), issue.getCustomerKey(), now, end);
        studentRepository.save(s);
        return mapToResponse(s);
    }

    @Transactional
    public void requestCancellation(String email) {
        Student s = findStudentByEmail(email);
        if (s.getSubscriptionStatus() != Student.SubscriptionStatus.ACTIVE) {
            throw new RuntimeException("Not active");
        }
//        tossPaymentsClient.unsubscribe(s.getBillingKey());
        s.requestCancellation();
        studentRepository.save(s);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getCurrent(String email) {
        Student s = findStudentByEmail(email);
        return mapToResponse(s);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionHistoryResponse> getHistory(String email) {
        Student s = findStudentByEmail(email);
        return historyRepository.findByStudentOrderByRequestedAtDesc(s).stream()
                .map(this::mapToHistory)
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void renewSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        List<Student> list = studentRepository.findBySubscriptionStatusAndSubscriptionEndBefore(Student.SubscriptionStatus.ACTIVE, now);
        for (Student s : list) {
            if (s.getBillingKey() == null) continue;
            try {
                triggerRenewalPayment(s, s.getBillingKey(), s.getCustomerKey(), s.getSubscriptionFee(), false);
                s.renewSubscription(s.getSubscriptionStart(), s.getSubscriptionStart().plusMonths(1));
                studentRepository.save(s);
            } catch (Exception e) {
                log.error("Renew failed {}: {}", s.getId(), e.getMessage(), e);
            }
        }
    }

    private void triggerRenewalPayment(Student s, String billingKey, String customerKey, BigDecimal fee, boolean isInitial) {
        String orderId = (isInitial?"init_":"sub_") + s.getId() + "_" + UUID.randomUUID();
        LocalDateTime reqAt = LocalDateTime.now();
        SubscriptionHistory h = SubscriptionHistory.builder()
                .student(s)
                .orderId(orderId)
                .subscriptionPeriodStart(reqAt)
                .subscriptionPeriodEnd(reqAt.plusMonths(1))
                .amount(fee)
                .status(SubscriptionHistory.Status.PENDING)
                .requestedAt(reqAt)
                .build();
        historyRepository.save(h);

        try {
            TossBillingApprovalRequest payReq = TossBillingApprovalRequest.builder()
                    .amount(fee)
                    .customerKey(customerKey)
                    .orderId(orderId)
                    .customerEmail(s.getEmail())
                    .build();
            TossPaymentResponse resp = tossPaymentsClient.approveBillingPayment(billingKey, payReq);
            if ("DONE".equalsIgnoreCase(resp.getStatus())) {
                h.setStatus(SubscriptionHistory.Status.SUCCESS);
                h.setPaymentKey(resp.getPaymentKey());
                h.setApprovedAt(resp.getApprovedAt());
            } else {
                h.setStatus(SubscriptionHistory.Status.FAILURE);
                h.setFailureCode(resp.getFailure()!=null?resp.getFailure().getCode():null);
                h.setFailureMessage(resp.getFailure()!=null?resp.getFailure().getMessage():null);
                throw new RuntimeException("Payment failed");
            }
        } catch (Exception ex) {
            h.setStatus(SubscriptionHistory.Status.FAILURE);
            h.setFailureMessage(ex.getMessage());
            throw ex;
        } finally {
            historyRepository.save(h);
        }
    }

    private SubscriptionResponse mapToResponse(Student s) {
        return SubscriptionResponse.builder()
                .studentId(s.getId())
                .customerKey(s.getCustomerKey())
                .status(s.getSubscriptionStatus().name())
                .startDate(s.getSubscriptionStart())
                .endDate(s.getSubscriptionEnd())
                .build();
    }

    private SubscriptionHistoryResponse mapToHistory(SubscriptionHistory h) {
        return SubscriptionHistoryResponse.builder()
                .historyId(h.getId())
                .orderId(h.getOrderId())
                .paymentKey(h.getPaymentKey())
                .status(h.getStatus().name())
                .periodStart(h.getSubscriptionPeriodStart())
                .periodEnd(h.getSubscriptionPeriodEnd())
                .amount(h.getAmount())
                .requestedAt(h.getRequestedAt())
                .approvedAt(h.getApprovedAt())
                .failureMessage(h.getFailureMessage())
                .build();
    }
}