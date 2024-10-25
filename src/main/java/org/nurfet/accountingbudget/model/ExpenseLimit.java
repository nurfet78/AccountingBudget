package org.nurfet.accountingbudget.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ExpenseLimit extends AbstractEntity {

    @NotNull(message = "Лимит должен быть указан")
    @DecimalMin(value = "0.01", inclusive = true, message = "Лимит должен быть больше нуля")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private LimitPeriod period;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate nextResetDate;

    public LocalDate setLimitPeriod(LimitPeriod period, LocalDate startDate) {
        this.period = period;
        this.startDate = startDate;

        return switch (period) {
            case WEEKLY -> this.endDate = startDate.plusWeeks(1).minusDays(1);
            case MONTHLY -> this.endDate = startDate.plusMonths(1).minusDays(1);
            case INDEFINITE -> this.endDate = null;
        };
    }

    @Getter
    public enum LimitPeriod {
        MONTHLY("Месяц"), WEEKLY("Неделя"), INDEFINITE("Бессрочно");

        private final String title;

        LimitPeriod(String title) {
            this.title = title;
        }
    }

    public boolean isExpired() {
        LocalDate today = LocalDate.now();

        if (this.period == LimitPeriod.INDEFINITE) {
            return false; // Бессрочный лимит никогда не истекает
        }

        return today.isAfter(this.endDate);
    }

    //для веб-интерфейса
    public String getPeriodDisplayName() {
        return switch (this.period) {
            case MONTHLY -> "В месяц";
            case WEEKLY -> "В неделю";
            case INDEFINITE -> "Бессрочно";
        };
    }
}
