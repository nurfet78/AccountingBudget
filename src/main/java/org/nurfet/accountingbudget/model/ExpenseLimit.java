package org.nurfet.accountingbudget.model;

import jakarta.persistence.Column;
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
    @DecimalMin(value = "0.01", message = "Лимит должен быть больше нуля")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private LimitPeriod period;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private boolean autoRenew = false; // По умолчанию автопродление выключено

    public void setLimitPeriod(LimitPeriod period, LocalDate startDate) {
        this.period = period;
        this.startDate = startDate;
        this.endDate = calculateEndDate(startDate, period);
    }

    private LocalDate calculateEndDate(LocalDate startDate, LimitPeriod period) {
        if (startDate == null || period == null) {
            return null;
        }

        return switch (period) {
            case WEEKLY -> startDate.plusWeeks(1).minusDays(1);
            case MONTHLY -> startDate.plusMonths(1).minusDays(1);
            case INDEFINITE -> null;
        };
    }

    public void updateDates(LocalDate newStartDate) {
        setLimitPeriod(this.period, newStartDate);
    }

    @Getter
    public enum LimitPeriod {
        MONTHLY("Месяц"), WEEKLY("Неделя"), INDEFINITE("Бессрочно");

        private final String title;

        LimitPeriod(String title) {
            this.title = title;
        }
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
