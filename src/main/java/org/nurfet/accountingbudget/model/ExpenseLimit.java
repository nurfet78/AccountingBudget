package org.nurfet.accountingbudget.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private LimitPeriod period;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate nextResetDate;

    public void setLimitPeriod(LimitPeriod period, LocalDate startDate) {
        this.period = period;
        this.startDate = startDate;

        switch (period) {
            case WEEKLY:
                this.endDate = startDate.plusWeeks(1).minusDays(1);
                break;
            case MONTHLY:
                this.endDate = startDate.plusMonths(1).minusDays(1);
                break;
            case INDEFINITE:
                this.endDate = null;
                break;
        }
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
            default -> "Неизвестно";
        };
    }
}
