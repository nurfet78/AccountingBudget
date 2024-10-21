package org.nurfet.accountingbudget.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ExpenseLimit extends AbstractEntity {

    private double amount;

    @Enumerated(EnumType.STRING)
    private LimitPeriod period;

    private LocalDate startDate;

    private LocalDate nextResetDate;

    public void updateNextResetDate() {
        if (this.period == LimitPeriod.WEEKLY) {
            this.nextResetDate = this.startDate.plusWeeks(1);
        } else if (this.period == LimitPeriod.MONTHLY) {
            this.nextResetDate = this.startDate.plusMonths(1).withDayOfMonth(1);
        } else {
            this.nextResetDate = null; // Для бессрочного лимита
        }
        System.out.println("Вызван метод updateNextResetDate. Дата начала: " + this.startDate + ", Дата следующего сброса: " + this.nextResetDate);
    }

    public enum LimitPeriod {
        MONTHLY, WEEKLY, INDEFINITE
    }

    public String getPeriodDisplayName() {
        return switch (this.period) {
            case MONTHLY -> "В месяц";
            case WEEKLY -> "В неделю";
            case INDEFINITE -> "Бессрочно";
            default -> "Неизвестно";
        };
    }

    public LocalDate getStartDateForPreviousPeriod() {
        return switch (this.period) {
            case WEEKLY -> this.startDate.minusWeeks(1);
            case MONTHLY -> this.startDate.minusMonths(1);
            case INDEFINITE -> this.startDate;
        };
    }
}
