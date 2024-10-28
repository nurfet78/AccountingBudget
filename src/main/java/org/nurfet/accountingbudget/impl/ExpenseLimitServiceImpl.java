package org.nurfet.accountingbudget.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.accountingbudget.model.ExpenseLimit;
import org.nurfet.accountingbudget.model.ExpenseLimit.LimitPeriod;
import org.nurfet.accountingbudget.observer.model.SendMessage;
import org.nurfet.accountingbudget.observer.service.MailEventPublisherService;
import org.nurfet.accountingbudget.repository.ExpenseLimitRepository;
import org.nurfet.accountingbudget.service.ExpenseLimitService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseLimitServiceImpl implements ExpenseLimitService {

    private final ExpenseLimitRepository expenseLimitRepository;

    private final MailEventPublisherService mailEventPublisherService;


    @Override
    public void setOrUpdateLimit(BigDecimal amount, ExpenseLimit.LimitPeriod period, boolean autoRenew) {
        ExpenseLimit limit = getCurrentLimit();
        if (limit == null) {
            limit = new ExpenseLimit();
        }
        limit.setAmount(amount);
        limit.setLimitPeriod(period, LocalDate.now());
        limit.setAutoRenew(autoRenew);
        expenseLimitRepository.save(limit);
    }

    @Override
    public void checkAndResetLimitIfNeeded() {
        LocalDate currentDate = getCurrentDate();

        List<ExpenseLimit> limits = expenseLimitRepository.findCurrentAndFutureLimits(currentDate);

        // Сортируем лимиты по дате начала
        limits.sort(Comparator.comparing(ExpenseLimit::getStartDate));

        // Находим текущий лимит (первый по дате начала)
        ExpenseLimit currentLimit = limits.isEmpty() ? null : limits.get(0);

        // Находим будущий лимит (второй по дате начала, если есть)
        ExpenseLimit futureLimit = limits.size() > 1 ? limits.get(1) : null;

        if (currentLimit == null) {
            log.info("Нет активного лимита.");
            return;
        }

        // Предположим, у нас есть:
        // 1. Текущий лимит (1000) с автопродлением: 2024-10-27 - 2024-11-02
        // 2. Будущий лимит (500): 2024-11-03 - 2024-11-09
        if (currentDate.isAfter(currentLimit.getEndDate()) || currentDate.isEqual(currentLimit.getEndDate())) {
            // Когда наступает 2024-11-03:
            // currentDate = 2024-11-03
            // currentLimit.getEndDate() = 2024-11-02
            // Условие выполняется (2024-11-03 > 2024-11-02)
            if (futureLimit != null) { // Будущий лимит имеет приоритет над автопродлением
                // futureLimit существует (500), поэтому выполняется эта ветка
                // Активируется будущий лимит, независимо от того,
                // что у текущего лимита включено автопродление
                log.info("Активация будущего лимита: {}", futureLimit);
                resetAndRenewLimit(futureLimit, currentDate, true);

                // Эта ветка не выполнится, потому что есть будущий лимит
            } else if (currentLimit.isAutoRenew()) {
                log.info("Автоматическое обновление текущего лимита: {}", currentLimit);
                resetAndRenewLimit(currentLimit, currentDate, false);
            } else {
                log.info("Лимит истек без автопродления и будущего лимита: {}", currentLimit);
                sendExpirationNotification(currentLimit);
                expenseLimitRepository.delete(currentLimit);
                log.info("Удален истекший лимит: {}", currentLimit);
            }
        } else {
            log.info("Нет необходимости сбрасывать или обновлять лимит. Текущий лимит все еще активен.");
        }
    }

    private void resetAndRenewLimit(ExpenseLimit limit, LocalDate newStartDate, boolean isNewLimit) {

        LocalDate oldStartDate = limit.getStartDate();
        LocalDate oldEndDate = limit.getEndDate();

        limit.setStartDate(newStartDate);
        limit.setEndDate(calculateEndDate(newStartDate, limit.getPeriod()));
        expenseLimitRepository.save(limit);


        if (isNewLimit) {
            sendNewLimitActivationNotification(limit);
        } else {
            sendRenewalNotification(limit, oldStartDate, oldEndDate, newStartDate);
        }
    }

    @Override
    public ExpenseLimit getCurrentLimit() {
        LocalDate today = getCurrentDate();

        List<ExpenseLimit> limits = expenseLimitRepository.findCurrentAndFutureLimits(today);

        ExpenseLimit currentLimit = limits.stream()
                .filter(limit -> !today.isBefore(limit.getStartDate()) && !today.isAfter(limit.getEndDate()))
                .findFirst()
                .orElse(null);

        if (currentLimit == null && !limits.isEmpty()) {
            currentLimit = limits.getFirst(); // Берем последний истекший лимит
        }

        return currentLimit;
    }


    @Override
    public ExpenseLimit getFutureLimit() {
        LocalDate today = getCurrentDate();

        List<ExpenseLimit> limits = expenseLimitRepository.findCurrentAndFutureLimits(today);

        return limits.stream()
                .filter(limit -> today.isBefore(limit.getStartDate()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void replaceFutureLimit(BigDecimal amount, ExpenseLimit.LimitPeriod period, boolean autoRenew) {
        ExpenseLimit futureLimit = getFutureLimit();
        if (futureLimit != null) {
            futureLimit.setAmount(amount);
            futureLimit.setPeriod(period);
            futureLimit.setEndDate(calculateEndDate(futureLimit.getStartDate(), period));
            expenseLimitRepository.save(futureLimit);
        } else {
            setFutureLimitAmount(amount, period, autoRenew);
        }
    }

    @Override
    public void setFutureLimitAmount(BigDecimal amount, LimitPeriod period, boolean autoRenew) {
        ExpenseLimit currentLimit = getCurrentLimit();
        if (currentLimit == null) {
            throw new IllegalStateException("Невозможно установить будущий лимит, так как текущий лимит отсутствует");
        }

        LocalDate nextPeriodStartDate = currentLimit.getEndDate().plusDays(1);

        ExpenseLimit futureLimit = new ExpenseLimit();
        futureLimit.setAmount(amount);
        futureLimit.setLimitPeriod(period, nextPeriodStartDate);
        futureLimit.setAutoRenew(autoRenew);

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
        String overrideDate = System.getProperty("expense-limit.current-date");
        return (overrideDate != null && !overrideDate.isEmpty())
                ? LocalDate.parse(overrideDate)
                : LocalDate.now();
    }

    private LocalDate calculateEndDate(LocalDate startDate, LimitPeriod period) {
        return switch (period) {
            case WEEKLY -> startDate.plusWeeks(1).minusDays(1);
            case MONTHLY -> startDate.plusMonths(1).minusDays(1);
            case INDEFINITE -> null;
        };
    }

    private void sendNewLimitActivationNotification(ExpenseLimit newLimit) {
        String message = String.format(
                "Начал действовать новый лимит расходов в размере %.2f. " +
                        "Тип периода: '%s'. Период: %s - %s.",
                newLimit.getAmount(),
                newLimit.getPeriod().getTitle(),
                newLimit.getStartDate(), newLimit.getEndDate()
        );
        mailEventPublisherService.publishMailCreatedEvent(SendMessage.create(message));
    }

    private void sendRenewalNotification(ExpenseLimit limit, LocalDate oldStartDate, LocalDate oldEndDate, LocalDate newStartDate) {
        String message = String.format(
                "Ваш лимит расходов в размере %.2f был автоматически продлен. " +
                        "Тип периода: '%s'. Старый период: %s - %s. Новый период начался с %s. Автопродление: %s.",
                limit.getAmount(),
                limit.getPeriod().getTitle(),
                oldStartDate, oldEndDate, newStartDate,
                limit.isAutoRenew() ? "включено" : "выключено"
        );
        mailEventPublisherService.publishMailCreatedEvent(SendMessage.create(message));
    }

    private void sendExpirationNotification(ExpenseLimit limit) {
        String message = String.format(
                "Ваш лимит расходов в размере %.2f истек. " +
                        "Тип периода: '%s'. Период: %s - %s. " +
                        "Для установки нового лимита, пожалуйста, войдите в систему.",
                limit.getAmount(),
                limit.getPeriod().getTitle(),
                limit.getStartDate(), limit.getEndDate()
        );

        mailEventPublisherService.publishMailCreatedEvent(SendMessage.create(message));
    }
}
