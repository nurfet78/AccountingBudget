package org.nurfet.accountingbudget.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.nurfet.accountingbudget.model.ExpenseLimit;
import org.nurfet.accountingbudget.observer.model.SendMessage;
import org.nurfet.accountingbudget.observer.service.MailEventPublisherService;
import org.nurfet.accountingbudget.repository.ExpenseLimitRepository;
import org.nurfet.accountingbudget.service.ExpenseLimitService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseLimitServiceImpl implements ExpenseLimitService {

    private final ExpenseLimitRepository expenseLimitRepository;

    private final MailEventPublisherService mailEventPublisherService;

    @Value("${expense-limit.current-date:#{null}}")
    private String currentDateOverride;

    private LocalDate getCurrentDate() {
        String override = System.getProperty("expense-limit.current-date");
        if (override != null && !override.isEmpty()) {
            try {
                return LocalDate.parse(override);
            } catch (DateTimeParseException e) {
                System.out.println("Ошибка при разборе значения expense-limit.current-date: " + e.getMessage());
            }
        }
        return LocalDate.now();
    }

    @Override
    public ExpenseLimit getCurrentLimit() {
        return expenseLimitRepository.findTopByOrderByStartDateDesc().orElse(null);
    }

    @Override
    public ExpenseLimit setLimit(double amount, ExpenseLimit.LimitPeriod period) {
        ExpenseLimit limit = new ExpenseLimit();
        limit.setAmount(amount);
        limit.setPeriod(period);
        limit.setStartDate(LocalDate.now());
        limit.updateNextResetDate();
        return expenseLimitRepository.save(limit);
    }

    @Override
    public void removeLimit() {
        ExpenseLimit currentLimit = getCurrentLimit();
        if (currentLimit != null) {
            expenseLimitRepository.delete(currentLimit);
        }
    }

    @Override
    public void checkAndUpdateAllLimits() {
        LocalDate now = getCurrentDate();
        System.out.println("Текущая дата в checkAndUpdateAllLimits: " + now);
        for (ExpenseLimit limit : expenseLimitRepository.findAll()) {
            System.out.println("Проверка лимита: Дата начала=" + limit.getStartDate() + ", NextResetDate=" + limit.getNextResetDate());
            if (shouldResetLimit(now, limit)) {
                LocalDate oldResetDate = limit.getNextResetDate();
                limit.setStartDate(now);
                limit.updateNextResetDate();
                expenseLimitRepository.save(limit);

                System.out.println("Лимит обновлен: Дата начала=" + limit.getStartDate() + ", Дата следующего сброса=" + limit.getNextResetDate());

                String message = String.format(
                        "Ваш лимит расходов в размере %.2f был сброшен. " +
                                "Тип периода: %s. Старый период: %s - %s. Новый период начался с %s.",
                        limit.getAmount(),
                        limit.getPeriod().toString(),
                        limit.getStartDateForPreviousPeriod(), oldResetDate, now
                );
                mailEventPublisherService.publishMailCreatedEvent(SendMessage.create(message));
            } else {
                System.out.println("Лимит не обновлен: Текущая дата не соответствует условиям сброса");
            }
        }
    }

    private boolean shouldResetLimit(LocalDate now, ExpenseLimit limit) {
        if (limit.getPeriod() == ExpenseLimit.LimitPeriod.MONTHLY) {
            return now.isAfter(limit.getStartDate()) &&
                    (now.isAfter(limit.getNextResetDate()) ||
                            now.isEqual(limit.getNextResetDate()) ||
                            now.getDayOfMonth() == now.lengthOfMonth() ||  // Добавлено условие для конца месяца
                            now.getMonth() != limit.getStartDate().getMonth());
        } else {
            return now.isAfter(limit.getStartDate()) &&
                    (now.isAfter(limit.getNextResetDate()) || now.isEqual(limit.getNextResetDate()));
        }
    }
}
