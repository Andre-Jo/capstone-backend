package com.muje.capstone.service;

import com.muje.capstone.domain.Student;
import com.muje.capstone.domain.SubscriptionHistory;
import com.muje.capstone.dto.*;
import com.muje.capstone.repository.SubscriptionHistoryRepository;
import com.muje.capstone.repository.UserRepository;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.ScheduleData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final IamportClient iamportClient;
    private final SubscriptionHistoryRepository historyRepo;
    private final UserRepository userRepo;

    @Transactional
    public SubscriptionResponse subscribe(String email, SubscriptionRequest req) throws IOException, IamportResponseException {
        Student student = (Student) userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getSubscribed()) throw new RuntimeException("Already subscribed");

        IamportResponse<Payment> resp = iamportClient.paymentByImpUid(req.getImpUid());
        if (resp.getCode() != 0 || resp.getResponse() == null) {
            throw new RuntimeException("Payment verification failed");
        }
        Payment p = resp.getResponse();

        BigDecimal amount = p.getAmount();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMonths(1);

        SubscriptionHistory hist = SubscriptionHistory.builder()
                .student(student)
                .startDate(start)
                .endDate(end)
                .status(SubscriptionHistory.Status.COMPLETED)
                .transactionId(p.getImpUid())
                .amount(amount)
                .build();
        historyRepo.save(hist);

        student.activateSubscription(start, end);
        userRepo.save(student);

        return SubscriptionResponse.builder()
                .id(hist.getId())
                .start(start)
                .end(end)
                .amount(amount)
                .transactionId(p.getImpUid())
                .build();
    }

    @Transactional
    public void cancel(String email) {
        Student student = (Student) userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // 빌링만 끊고 상태만 업데이트
        student.cancelSubscriptionButKeepActiveUntilExpiry();
        userRepo.save(student);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse current(String email) {
        Student student = (Student) userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return historyRepo.findTopByStudentOrderByStartDateDesc(student)
                .map(h -> SubscriptionResponse.builder()
                        .id(h.getId())
                        .start(h.getStartDate())
                        .end(h.getEndDate())
                        .amount(h.getAmount())
                        .transactionId(h.getTransactionId())
                        .build())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> history(String email) {
        Student student = (Student) userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return historyRepo.findByStudentOrderByStartDateDesc(student)
                .stream()
                .map(h -> SubscriptionResponse.builder()
                        .id(h.getId())
                        .start(h.getStartDate())
                        .end(h.getEndDate())
                        .amount(h.getAmount())
                        .transactionId(h.getTransactionId())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void renew(String email) throws IamportResponseException, IOException {
        Student student = (Student) userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getBillingKey() == null) {
            throw new RuntimeException("No billing key found for user: " + email);
        }

        // 1. 결제 요청 준비
        String billingKey = student.getBillingKey();
        String merchantUid = "sub_" + UUID.randomUUID(); // 고유한 merchant_uid
        BigDecimal amount = String.valueOf(new BigDecimal("5000")); // 정기결제 금액 (원하는 가격으로 수정)

        // 2. 아임포트 정기결제 API 호출
        ScheduleData schedule = new ScheduleData(billingKey, merchantUid, amount);
        schedule.setCustomerUid(billingKey);

        IamportResponse<Payment> resp = iamportClient.subscribeAgain(schedule);

        if (resp.getCode() != 0 || resp.getResponse() == null) {
            throw new RuntimeException("정기 결제 실패: " + resp.getMessage());
        }

        Payment payment = resp.getResponse();

        // 3. 결제 성공 처리
        LocalDateTime start = student.getSubscriptionEndDate().isAfter(LocalDateTime.now())
                ? student.getSubscriptionEndDate()
                : LocalDateTime.now();
        LocalDateTime end = start.plusMonths(1);

        SubscriptionHistory history = SubscriptionHistory.builder()
                .student(student)
                .startDate(start)
                .endDate(end)
                .status(SubscriptionHistory.Status.COMPLETED)
                .transactionId(payment.getImpUid())
                .amount(payment.getAmount())
                .build();
        historyRepo.save(history);

        student.activateSubscription(start, end);
        userRepo.save(student);
    }

}