package org.nurfet.accountingbudget.service;

import org.nurfet.accountingbudget.model.ExpenseLimit;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExpenseLimitService {

    void setOrUpdateLimit(BigDecimal amount, ExpenseLimit.LimitPeriod period);

    void checkAndResetLimitIfNeeded();

    boolean shouldResetLimit(LocalDate now, ExpenseLimit limit);

    ExpenseLimit getCurrentLimit();

    void removeLimit();
}
