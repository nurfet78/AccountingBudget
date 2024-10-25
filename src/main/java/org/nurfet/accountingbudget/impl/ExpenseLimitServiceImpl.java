package org.nurfet.accountingbudget.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.accountingbudget.model.ExpenseLimit;
import org.nurfet.accountingbudget.model.ExpenseLimit.LimitPeriod;
import org.nurfet.accountingbudget.observer.model.SendMessage;
import org.nurfet.accountingbudget.observer.service.MailEventPublisherService;
import org.nurfet.accountingbudget.repository.ExpenseLimitRepository;
import org.nurfet.accountingbudget.service.ExpenseLimitService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseLimitServiceImpl implements ExpenseLimitService {

    private final ExpenseLimitRepository expenseLimitRepository;

    private final MailEventPublisherService mailEventPublisherService;

    private final Environment environment;


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
        LocalDate today = getCurrentDate();
        ExpenseLimit currentLimit;

        if (isTestEnvironment()) {
            // Для тестового окружения
            currentLimit = expenseLimitRepository.findTopByOrderByStartDateDesc().orElse(null);
        } else {
            // Для обычного запуска
            currentLimit = expenseLimitRepository.findCurrentLimit(today);

            if (currentLimit == null) {
                // Если текущий лимит не найден, проверяем, есть ли будущий лимит
                ExpenseLimit futureLimit = getFutureLimit();
                if (futureLimit != null && !futureLimit.getStartDate().isAfter(today)) {
                    // Если будущий лимит есть и его дата начала не позже сегодняшней,
                    // делаем его текущим
                    futureLimit.setStartDate(today);
                    futureLimit.setEndDate(calculateEndDate(today, futureLimit.getPeriod()));
                    currentLimit = expenseLimitRepository.save(futureLimit);
                }
            }
        }

        return currentLimit;
    }

    @Override
    public ExpenseLimit getFutureLimit() {
        LocalDate today = getCurrentDate();
        return expenseLimitRepository.findFutureLimit(today);
    }

    @Override
    public void replaceFutureLimit(BigDecimal amount, ExpenseLimit.LimitPeriod period) {
        ExpenseLimit futureLimit = getFutureLimit();
        if (futureLimit != null) {
            futureLimit.setAmount(amount);
            futureLimit.setPeriod(period);
            futureLimit.setEndDate(calculateEndDate(futureLimit.getStartDate(), period));
            expenseLimitRepository.save(futureLimit);
        } else {
            setFutureLimitAmount(amount, period);
        }
    }

    @Override
    public void setFutureLimitAmount(BigDecimal amount, LimitPeriod period) {
        ExpenseLimit currentLimit = getCurrentLimit();
        if (currentLimit == null) {
            throw new IllegalStateException("Невозможно установить будущий лимит, так как текущий лимит отсутствует");
        }

        LocalDate nextPeriodStartDate = currentLimit.getEndDate().plusDays(1);

        ExpenseLimit futureLimit = new ExpenseLimit();
        futureLimit.setAmount(amount);
        futureLimit.setPeriod(period);
        futureLimit.setStartDate(nextPeriodStartDate);
        futureLimit.setEndDate(calculateEndDate(nextPeriodStartDate, period));

        expenseLimitRepository.save(futureLimit);
    }

    @Override
    public void removeLimit() {
        ExpenseLimit currentLimit = getCurrentLimit();
        if (currentLimit != null) {
            expenseLimitRepository.delete(currentLimit);

            // После удаления текущего лимита, проверяем есть ли будущий лимит
            ExpenseLimit futureLimit = getFutureLimit();
            if (futureLimit != null) {
                // Если будущий лимит есть, делаем его текущим
                LocalDate today = getCurrentDate();
                if (futureLimit.getStartDate().isAfter(today)) {
                    futureLimit.setStartDate(today);
                    futureLimit.setEndDate(calculateEndDate(today, futureLimit.getPeriod()));
                    expenseLimitRepository.save(futureLimit);
                }
            }
        }
    }

    private LocalDate getCurrentDate() {
        String override = System.getProperty("expense-limit.current-date"); //Свойство устанавливается в тестах
        return (override != null && !override.isEmpty()) ? LocalDate.parse(override) : LocalDate.now();
    }

    private LocalDate calculateEndDate(LocalDate startDate, LimitPeriod period) {
        return switch (period) {
            case WEEKLY -> startDate.plusWeeks(1).minusDays(1);
            case MONTHLY -> startDate.plusMonths(1).minusDays(1);
            case INDEFINITE -> null;
        };
    }

    private boolean isTestEnvironment() {
        return Arrays.asList(environment.getActiveProfiles()).contains("test");
    }
}
