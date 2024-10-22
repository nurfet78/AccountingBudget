package org.nurfet.accountingbudget.config;

import lombok.RequiredArgsConstructor;
import org.nurfet.accountingbudget.service.ExpenseLimitService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpenseLimitScheduler {

    private final ExpenseLimitService expenseLimitService;

    @Scheduled(cron = "0 0 0 * * ?") // Выполняется каждый день в полночь
    public void checkAndUpdateLimits() {
        expenseLimitService.checkAndResetLimitIfNeeded();
    }
}
