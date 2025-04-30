package com.muje.capstone.config;

import com.muje.capstone.domain.SubscriptionHistory;
import com.muje.capstone.repository.SubscriptionHistoryRepository;
import com.muje.capstone.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillingScheduler {

    private final SubscriptionHistoryRepository historyRepo;
    private final SubscriptionService subscriptionService;

    /**
     * 매일 자정(00:00)에 정기 결제 대상 구독을 갱신
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void renewSubscriptions() {
        LocalDateTime now = LocalDateTime.now();

        // 1) 만료일이 오늘 이전이거나 당일인 ACTIVE 구독 찾기
        List<SubscriptionHistory> due = historyRepo.findAllByStatusAndEndDateBefore(
                SubscriptionHistory.Status.COMPLETED, LocalDateTime.now().plusDays(1)
        );

        for (SubscriptionHistory hist : due) {
            try {
                // 2) 실제 결제 실행 (SubscriptionService 내부에 charge 로직 구현)
                subscriptionService.renew(hist.getStudent().getEmail());
                // 3) 성공 시 이력과 student 갱신은 service 내에서 처리
            } catch (Exception ex) {
                // 실패 로깅, 관리자 알림 등 추가 처리
                log.error("정기 결제 실패: subscriptionId={} user={}",
                        hist.getId(), hist.getStudent().getEmail(), ex);
            }
        }
    }
}
