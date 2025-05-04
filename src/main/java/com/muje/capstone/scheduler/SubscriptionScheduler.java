package com.muje.capstone.scheduler;

import com.muje.capstone.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j // Use Slf4j for logging
public class SubscriptionScheduler {

    private final SubscriptionService subscriptionService;

    // Example: Run daily at 3 AM
    // Cron expression: second, minute, hour, day of month, month, day(s) of week
    // See: https://docs.oracle.com/cd/E12058_01/doc/doc.1014/e12030/cron_expressions.htm
//    @Scheduled(cron = "0 0 3 * * ?") // Runs at 3:00:00 AM every day
//    @Scheduled(fixedRate = 10000)
    // For testing, you might use: @Scheduled(fixedRate = 60000) // Run every 60 seconds
    public void runDailySubscriptionRenewal() {
        log.info("Starting daily subscription renewal job...");
        try {
            subscriptionService.renewAndExpire();
            log.info("Finished daily subscription renewal job.");
        } catch (Exception e) {
            // Log the error, but don't let it stop the scheduler from running next time
            log.error("Error during scheduled subscription renewal: {}", e.getMessage(), e);
        }
    }
}