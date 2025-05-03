package com.muje.capstone.service;

import com.muje.capstone.config.TossPaymentsClient;
import com.muje.capstone.domain.Student;
import com.muje.capstone.domain.SubscriptionHistory;
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

    private Student findStudent(String email) {
        return userRepository.findByEmail(email)
                .filter(u -> u instanceof Student)
                .map(u -> (Student)u)
                .orElseThrow(() -> new RuntimeException("Student not found: " + email));
    }

    /** 카드 등록 → 첫 결제 → 10초 구독 */
    @Transactional
    public SubscriptionResponse registerBillingKeyAndSubscribe(String email, BillingKeyRequest req) {
        Student s = findStudent(email);
        if (s.isSubscriptionActive()) throw new RuntimeException("Already subscribed");

        var issue = tossPaymentsClient.issueBillingKey(req.getAuthKey(), req.getCustomerKey());
        BigDecimal fee = req.getAmount() != null ? req.getAmount() : s.getSubscriptionFee();

        // 첫 결제
        executePayment(s, issue.getBillingKey(), issue.getCustomerKey(), fee, true);

        // 활성화
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end   = start.plusSeconds(10);
        s.activateSubscription(fee, issue.getBillingKey(), issue.getCustomerKey(), start, end);
        studentRepository.save(s);

        return new SubscriptionResponse(s.getId(), s.getCustomerKey(),
                s.getSubscriptionStatus().name(), start, end);
    }

    /** 사용자의 “취소” 요청 */
    @Transactional
    public void requestCancellation(String email) {
        Student s = findStudent(email);
        if (!s.isSubscriptionActive()) throw new RuntimeException("Not active");

        s.requestCancellation();
        studentRepository.save(s);
    }

    /** 현재 구독 상태 조회 */
    @Transactional(readOnly = true)
    public SubscriptionResponse getCurrent(String email) {
        Student s = findStudent(email);
        return new SubscriptionResponse(
                s.getId(), s.getCustomerKey(),
                s.getSubscriptionStatus().name(),
                s.getSubscriptionStart(), s.getSubscriptionEnd()
        );
    }

    /** 구독 이력 조회 */
    @Transactional(readOnly = true)
    public List<SubscriptionHistoryResponse> getHistory(String email) {
        Student s = findStudent(email);
        return historyRepository.findByStudentOrderByRequestedAtDesc(s).stream()
                .map(h -> new SubscriptionHistoryResponse(
                        h.getId(), h.getOrderId(), h.getPaymentKey(), h.getStatus().name(),
                        h.getPeriodStart(), h.getPeriodEnd(), h.getAmount(),
                        h.getRequestedAt(), h.getApprovedAt(), h.getFailureMessage()
                ))
                .collect(Collectors.toList());
    }

    /** 10초마다, ACTIVE & 만료 시점 지난 구독만 갱신 **/
    @Transactional
    public void renewSubscriptions() {
        LocalDateTime now = LocalDateTime.now();

        // 만료일이 지난 ACTIVE 구독만 가져옴
        var list = studentRepository.findBySubscriptionStatusAndSubscriptionEndBefore(
                Student.SubscriptionStatus.ACTIVE, now
        );

        for (var s : list) {
            // 취소 요청된 사용자는 스킵
            if (s.getSubscriptionStatus() == Student.SubscriptionStatus.CANCELLATION_REQUESTED) {
                continue;
            }

            System.out.println(s);

            log.info("Renewing student {} with end date {}", s.getId(), s.getSubscriptionEnd());
            executePayment(s, s.getBillingKey(), s.getCustomerKey(), s.getSubscriptionFee(), false);
        }

        // 2) 만료 처리 대상: CANCELLATION_REQUESTED & 만료일 지난 사용자
        var expireList = studentRepository.findBySubscriptionStatusAndSubscriptionEndBefore(
                Student.SubscriptionStatus.CANCELLATION_REQUESTED, now);
        for (var s : expireList) {
            s.deactivateSubscription();
            s.setSubscriptionStart(null);
            s.setSubscriptionEnd(null);
            studentRepository.save(s);
            log.info("Expired cancellation for student {}", s.getId());
        }
    }

    /** Toss API 호출 + 히스토리 기록 **/
    private void executePayment(
            Student s,
            String billingKey,
            String customerKey,
            BigDecimal fee,
            boolean initial
    ) {
        LocalDateTime now = LocalDateTime.now();

        // 2) 주문 ID, 시작/종료 시점 계산
        String orderId = (initial ? "init_" : "sub_") + s.getId() + "_" + UUID.randomUUID();
        // periodStart: 이전 종료일이 있으면 그때, 없으면 지금(now)
        LocalDateTime periodStart = s.getSubscriptionEnd() != null
                ? s.getSubscriptionEnd()
                : now;

        if (periodStart.isBefore(now)) {
            periodStart = now;
        }

        // periodEnd: 시작일 + 10초
        LocalDateTime periodEnd   = periodStart.plusSeconds(10);

        // 3) 히스토리 초기 저장
        SubscriptionHistory h = SubscriptionHistory.builder()
                .student(s)
                .orderId(orderId)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .amount(fee)
                .status(SubscriptionHistory.Status.PENDING)
                .requestedAt(now)
                .build();
        historyRepository.save(h);

        try {
            // 4) Toss API 호출
            TossBillingApprovalRequest req = TossBillingApprovalRequest.builder()
                    .amount(fee)
                    .customerKey(customerKey)
                    .orderId(orderId)
                    .customerEmail(s.getEmail())
                    .build();
            TossPaymentResponse resp = tossPaymentsClient.approveBillingPayment(billingKey, req);
            if (!"DONE".equalsIgnoreCase(resp.getStatus())) {
                throw new RuntimeException("Payment failed: " + resp.getStatus());
            }

            // 5) 성공 처리
            h.setStatus(SubscriptionHistory.Status.SUCCESS);
            h.setPaymentKey(resp.getPaymentKey());
            h.setApprovedAt(resp.getApprovedAt());

            // 6) Student 엔티티 구독 시작/종료 갱신
            s.renewSubscription(periodStart, periodEnd);
            studentRepository.save(s);

        } catch (Exception ex) {
            // 실패 처리
            h.setStatus(SubscriptionHistory.Status.FAILURE);
            h.setFailureMessage(ex.getMessage());

            // 만약 자동 갱신 중 실패하면 → 비활성화
            if (!initial) {
                s.deactivateSubscription();
                studentRepository.save(s);
            }

            throw ex;
        } finally {
            historyRepository.save(h);
        }
    }
}