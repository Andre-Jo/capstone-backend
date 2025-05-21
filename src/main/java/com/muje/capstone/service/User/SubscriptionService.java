package com.muje.capstone.service.User;

import com.muje.capstone.config.TossPaymentsClient;
import com.muje.capstone.domain.User.Student;
import com.muje.capstone.domain.User.SubscriptionHistory;
import com.muje.capstone.dto.Payment.BillingKeyRequest;
import com.muje.capstone.dto.Payment.CardRegisterRequest;
import com.muje.capstone.dto.Payment.TossBillingApprovalRequest;
import com.muje.capstone.dto.User.Subscribe.ResumeSubscriptionRequest;
import com.muje.capstone.dto.User.Subscribe.SubscriptionHistoryResponse;
import com.muje.capstone.dto.User.Subscribe.SubscriptionResponse;
import com.muje.capstone.repository.User.StudentRepository;
import com.muje.capstone.repository.User.SubscriptionHistoryRepository;
import com.muje.capstone.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
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

    /** 카드 등록 → 첫 결제 → 구독 활성화 */
    @Transactional
    public SubscriptionResponse registerBillingKeyAndSubscribe(String email, BillingKeyRequest req) {
        Student s = findStudent(email);
        if (s.isSubscriptionActive()) {
            throw new RuntimeException("Already subscribed");
        }

        // 1) 빌링키 발급
        var issue = tossPaymentsClient.issueBillingKey(req.getAuthKey(), req.getCustomerKey());
        BigDecimal fee = req.getAmount();
        int periodDays = req.getPeriod();

        // 2) 결제 실행 (baseStart = now)
        LocalDateTime baseStart = LocalDateTime.now();
        executePayment(s, issue.getBillingKey(), issue.getCustomerKey(), fee, true, baseStart, periodDays);

        // 3) 구독 활성화 (만료일은 baseStart + periodDays 의 그날 자정)
        LocalDateTime start = baseStart;
        LocalDateTime end   = baseStart.plusDays(periodDays).with(LocalTime.MAX);
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

    /** 빌링키 등록 **/
    @Transactional
    public void saveBillingKey(String email, CardRegisterRequest req) {
        Student s = findStudent(email);
        var issue = tossPaymentsClient.issueBillingKey(req.getAuthKey(), req.getCustomerKey());
        s.setBillingKey(issue.getBillingKey());
        s.setCustomerKey(issue.getCustomerKey());
        studentRepository.save(s);
    }

    /** 빌링키 삭제 **/
    @Transactional
    public void deleteBillingKey(String email) {
        Student s = findStudent(email);

        if (s.getBillingKey() != null) {
            s.setBillingKey(null);
            s.setCustomerKey(null);
            studentRepository.save(s);
        }
    }

    /** 스케줄러: 자동 갱신 및 만료 처리 */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정
    @Transactional
    public void renewAndExpire() {
        LocalDateTime now = LocalDateTime.now();

        // 1) 갱신: ACTIVE & 만료 지남
        studentRepository
                .findBySubscriptionStatusAndSubscriptionEndBefore(Student.SubscriptionStatus.ACTIVE, now)
                .forEach(s -> {
                    if (s.getSubscriptionStatus() == Student.SubscriptionStatus.CANCELLATION_REQUESTED) return;
                    log.info("Auto-renew student {}", s.getId());

                    // baseStart = 이전 만료일이 future 면 그 값을, 아니면 now
                    LocalDateTime baseStart = s.getSubscriptionEnd().isAfter(now)
                            ? s.getSubscriptionEnd()
                            : now;
                    // 기간은 기존에 저장된 fee 대비 request 안 하므로 student.getSubscriptionFeePeriod() 필요시 저장해두세요
                    int periodDays = Period.between(s.getSubscriptionStart().toLocalDate(), s.getSubscriptionEnd().toLocalDate()).getDays();
                    executePayment(s, s.getBillingKey(), s.getCustomerKey(), s.getSubscriptionFee(), false, baseStart, periodDays);

                    // 연장: same as above
                    LocalDateTime start = baseStart;
                    LocalDateTime end   = baseStart.plusDays(periodDays).with(LocalTime.MAX);
                    s.activateSubscription(s.getSubscriptionFee(), s.getBillingKey(), s.getCustomerKey(), start, end);
                    studentRepository.save(s);
                });

        // 2) 만료: CANCELLATION_REQUESTED & 만료 지남
        studentRepository
                .findBySubscriptionStatusAndSubscriptionEndBefore(Student.SubscriptionStatus.CANCELLATION_REQUESTED, now)
                .forEach(s -> {
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
     * @param baseStart   이번 결제의 시작
     * @param periodDays  구독 기간(일)
     */
    private void executePayment(
            Student s,
            String billingKey,
            String customerKey,
            BigDecimal fee,
            boolean initial,
            LocalDateTime baseStart,
            int periodDays
    ) {
        LocalDateTime periodStart = baseStart;
        LocalDateTime periodEnd   = baseStart.plusDays(periodDays).with(LocalTime.MAX);
        LocalDateTime now         = LocalDateTime.now();
        String orderId = (initial ? "init_" : "sub_")
                + s.getId() + "_" + UUID.randomUUID();

        // 1) 히스토리 PENDING
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
            // 2) Toss 승인 호출
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

            // 3) SUCCESS 처리
            h.setStatus(SubscriptionHistory.Status.SUCCESS);
            h.setPaymentKey(resp.getPaymentKey());
            h.setApprovedAt(resp.getApprovedAt());
        } catch (Exception ex) {
            // 4) FAILURE 처리
            h.setStatus(SubscriptionHistory.Status.FAILURE);
            h.setFailureMessage(ex.getMessage());
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
    public SubscriptionResponse resumeSubscription(String email, ResumeSubscriptionRequest req) {
        Student s = findStudent(email);
        if (s.getSubscriptionStatus() != Student.SubscriptionStatus.CANCELLATION_REQUESTED
                && s.getSubscriptionStatus() != Student.SubscriptionStatus.INACTIVE) {
            throw new RuntimeException("Cannot resume in state: " + s.getSubscriptionStatus());
        }
        if (s.getBillingKey() == null) {
            throw new RuntimeException("No billing key for resume");
        }

        BigDecimal fee = req.getAmount();
        int periodDays = req.getPeriod();

        // baseStart 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime subEnd = s.getSubscriptionEnd();
        LocalDateTime baseStart = (subEnd != null && subEnd.isAfter(now))
                ? subEnd
                : now;

        // 즉시 결제
        executePayment(s, s.getBillingKey(), s.getCustomerKey(), fee, false, baseStart, periodDays);

        // 재활성화
        LocalDateTime start = baseStart;
        LocalDateTime end   = baseStart.plusDays(periodDays).with(LocalTime.MAX);
        s.activateSubscription(fee, s.getBillingKey(), s.getCustomerKey(), start, end);
        studentRepository.save(s);

        return new SubscriptionResponse(s.getId(), s.getCustomerKey(),
                s.getSubscriptionStatus().name(), start, end);
    }
}