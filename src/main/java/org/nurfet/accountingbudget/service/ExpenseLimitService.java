package org.nurfet.accountingbudget.service;

import org.nurfet.accountingbudget.model.ExpenseLimit;
import org.nurfet.accountingbudget.model.ExpenseLimit.LimitPeriod;

import java.math.BigDecimal;

public interface ExpenseLimitService {

    void setOrUpdateLimit(BigDecimal amount, ExpenseLimit.LimitPeriod period, boolean autoRenew);

    void checkAndResetLimitIfNeeded();

    ExpenseLimit getCurrentLimit();

    ExpenseLimit getFutureLimit();

    void setFutureLimitAmount(BigDecimal amount, LimitPeriod period, boolean autoRenew);

    void removeLimit();

    void replaceFutureLimit(BigDecimal amount, ExpenseLimit.LimitPeriod period, boolean autoRenew);
}
