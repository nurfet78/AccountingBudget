package org.nurfet.accountingbudget.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.accountingbudget.model.ExpenseLimit;
import org.nurfet.accountingbudget.observer.model.SendMessage;
import org.nurfet.accountingbudget.observer.service.MailEventPublisherService;
import org.nurfet.accountingbudget.repository.ExpenseLimitRepository;
import org.nurfet.accountingbudget.service.ExpenseLimitService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseLimitServiceImpl implements ExpenseLimitService {

    private final ExpenseLimitRepository expenseLimitRepository;

    private final MailEventPublisherService mailEventPublisherService;


    @Override
    public void setOrUpdateLimit(BigDecimal amount, ExpenseLimit.LimitPeriod period) {
        ExpenseLimit limit = getCurrentLimit();
        if (limit == null) {
            limit = new ExpenseLimit();
        }
        limit.setAmount(amount);
        limit.setLimitPeriod(period, LocalDate.now());
        expenseLimitRepository.save(limit);
    }

    @Override
    public void checkAndResetLimitIfNeeded() {
        ExpenseLimit limit = getCurrentLimit();
        if (limit == null) return;

        LocalDate currentDate = getCurrentDate();
        if (shouldResetLimit(currentDate, limit)) {
            LocalDate oldStartDate = limit.getStartDate();
            LocalDate oldEndDate = limit.getEndDate();

            LocalDate newStartDate = oldEndDate.plusDays(1);
            limit.setLimitPeriod(limit.getPeriod(), newStartDate);
            expenseLimitRepository.save(limit);

            String message = String.format(
                    "Ваш лимит расходов в размере %.2f был сброшен. " +
                            "Тип периода: '%s'. Старый период: %s - %s. Новый период начался с %s.",
                    limit.getAmount(),
                    limit.getPeriod().getTitle(),
                    oldStartDate, oldEndDate, newStartDate
            );
            mailEventPublisherService.publishMailCreatedEvent(SendMessage.create(message));
        }
    }

    @Override
    public boolean shouldResetLimit(LocalDate currentDate, ExpenseLimit limit) {
        if (limit.getPeriod() == ExpenseLimit.LimitPeriod.INDEFINITE) {
            return false;
        }
        return currentDate.isAfter(limit.getEndDate()) || currentDate.isEqual(limit.getEndDate());
    }

    @Override
    public ExpenseLimit getCurrentLimit() {
        return expenseLimitRepository.findTopByOrderByStartDateDesc().orElse(null);
    }

    @Override
    public void removeLimit() {
        ExpenseLimit currentLimit = getCurrentLimit();
        if (currentLimit != null) {
            expenseLimitRepository.delete(currentLimit);
        }
    }

    private LocalDate getCurrentDate() {
        String override = System.getProperty("expense-limit.current-date"); //Свойство устанавливается в тестах
        return (override != null && !override.isEmpty()) ? LocalDate.parse(override) : LocalDate.now();
    }
}
