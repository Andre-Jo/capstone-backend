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

    /** 구독 기간(초) */
    private static final long SUBSCRIPTION_PERIOD_SECONDS = 10L;

    private Student findStudent(String email) {
        return userRepository.findByEmail(email)
                .filter(u -> u instanceof Student)
                .map(u -> (Student)u)
                .orElseThrow(() -> new RuntimeException("Student not found: " + email));
    }

    /** 카드 등록 → 첫 결제 → 구독 활성화 */
    @Transactional
    public SubscriptionResponse registerBillingKeyAndSubscribe(String email, BillingKeyRequest req) {
        Student s = findStudent(email);
        if (s.isSubscriptionActive()) throw new RuntimeException("Already subscribed");

        // 1) 빌링키 발급
        var issue = tossPaymentsClient.issueBillingKey(req.getAuthKey(), req.getCustomerKey());
        BigDecimal fee = req.getAmount();

        // 2) 첫 결제
        executePayment(s, issue.getBillingKey(), issue.getCustomerKey(), fee, true);

        // 3) 활성화
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end   = start.plusSeconds(SUBSCRIPTION_PERIOD_SECONDS);
        s.activateSubscription(fee, issue.getBillingKey(), issue.getCustomerKey(), start, end);
        studentRepository.save(s);

        return new SubscriptionResponse(s.getId(), s.getCustomerKey(),
                s.getSubscriptionStatus().name(), start, end);
    }

    /** 구독 취소 요청 */
    @Transactional
    public void requestCancellation(String email) {
        Student s = findStudent(email);
        if (!s.isSubscriptionActive()) throw new RuntimeException("Not active");

        s.requestCancellation();
        studentRepository.save(s);
    }

    /** 현재 구독 조회 */
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

    /** 스케줄러: 자동 갱신 및 만료 처리 */
//    @Scheduled(fixedRate = 13000)
    @Scheduled(cron = "0 0 0 * * ?") // Runs at 12:00:00 AM every day
    @Transactional
    public void renewAndExpire() {
        LocalDateTime now = LocalDateTime.now();

        // 1) 자동 갱신 대상: ACTIVE & 만료 시점 지난 사용자
        var toRenew = studentRepository.findBySubscriptionStatusAndSubscriptionEndBefore(
                Student.SubscriptionStatus.ACTIVE, now);
        toRenew.forEach(s -> {
            if (s.getSubscriptionStatus() == Student.SubscriptionStatus.CANCELLATION_REQUESTED) return;
            log.info("Auto-renew student {}", s.getId());
            executePayment(s, s.getBillingKey(), s.getCustomerKey(), s.getSubscriptionFee(), false);
        });

        // 2) 만료 처리: CANCELLATION_REQUESTED & 만료 시점 지난 사용자
        var toExpire = studentRepository.findBySubscriptionStatusAndSubscriptionEndBefore(
                Student.SubscriptionStatus.CANCELLATION_REQUESTED, now);
        toExpire.forEach(s -> {
            s.deactivateSubscription();
            s.setSubscriptionStart(null);
            s.setSubscriptionEnd(null);
            s.setSubscriptionFee(BigDecimal.ZERO);
            studentRepository.save(s);
            log.info("Expired cancellation for student {}", s.getId());
        });
    }

    /**
     * Toss API 호출 + 히스토리 기록
     * @param s        학생 엔티티
     * @param billingKey billingKey
     * @param customerKey customerKey
     * @param fee      결제 금액
     * @param initial  최초 결제 여부
     */
    private void executePayment(
            Student s,
            String billingKey,
            String customerKey,
            BigDecimal fee,
            boolean initial
    ) {
        LocalDateTime now = LocalDateTime.now();

        // 1) 주문 ID, 기간 계산
        String orderId = (initial ? "init_" : "sub_") + s.getId() + "_" + UUID.randomUUID();
        LocalDateTime periodStart = now;
        LocalDateTime periodEnd   = now.plusSeconds(SUBSCRIPTION_PERIOD_SECONDS);

        // 2) 히스토리 PENDING 생성
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
            // 3) Toss 승인 호출
            var req = TossBillingApprovalRequest.builder()
                    .amount(fee)
                    .customerKey(customerKey)
                    .orderId(orderId)
                    .customerEmail(s.getEmail())
                    .build();
            var resp = tossPaymentsClient.approveBillingPayment(billingKey, req);
            if (!"DONE".equalsIgnoreCase(resp.getStatus())) {
                throw new RuntimeException("Payment failed: " + resp.getStatus());
            }

            // 4) SUCCESS 처리
            h.setStatus(SubscriptionHistory.Status.SUCCESS);
            h.setPaymentKey(resp.getPaymentKey());
            h.setApprovedAt(resp.getApprovedAt());

            // 5) student 기간 갱신
            s.renewSubscription(periodStart, periodEnd);
            studentRepository.save(s);

        } catch (Exception ex) {
            // 6) FAILURE 처리
            h.setStatus(SubscriptionHistory.Status.FAILURE);
            h.setFailureMessage(ex.getMessage());

            // 자동 갱신 중 실패하면 즉시 비활성화
            if (!initial) {
                s.deactivateSubscription();
                s.setSubscriptionStart(null);
                s.setSubscriptionEnd(null);
                s.setSubscriptionFee(BigDecimal.ZERO);
                studentRepository.save(s);
            }
            throw ex;
        } finally {
            historyRepository.save(h);
        }
    }

    /** CANCELLATION_REQUESTED 또는 INACTIVE → 재구독 */
    @Transactional
    public SubscriptionResponse resumeSubscription(String email, BillingKeyRequest req) {
        Student s = findStudent(email);

        // 1) 상태 검증
        if (s.getSubscriptionStatus() != Student.SubscriptionStatus.CANCELLATION_REQUESTED
                && s.getSubscriptionStatus() != Student.SubscriptionStatus.INACTIVE) {
            throw new RuntimeException("재구독할 수 없는 상태: " + s.getSubscriptionStatus());
        }
        // 2) 빌링키 검증
        if (s.getBillingKey() == null || s.getCustomerKey() == null) {
            throw new RuntimeException("재구독을 위한 빌링키 정보가 없습니다.");
        }

        BigDecimal fee = req.getAmount();

        // 3) 즉시 결제
        executePayment(s, s.getBillingKey(), s.getCustomerKey(), fee, false);

        // 4) 재활성화
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end   = start.plusSeconds(SUBSCRIPTION_PERIOD_SECONDS);
        s.activateSubscription(fee, s.getBillingKey(), s.getCustomerKey(), start, end);
        studentRepository.save(s);

        return new SubscriptionResponse(
                s.getId(), s.getCustomerKey(), s.getSubscriptionStatus().name(), start, end);
    }
}