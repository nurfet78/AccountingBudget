package org.nurfet.accountingbudget.service;

import org.nurfet.accountingbudget.model.ExpenseLimit;

public interface ExpenseLimitService {

    ExpenseLimit getCurrentLimit();

    ExpenseLimit setLimit(double amount, ExpenseLimit.LimitPeriod period);

    void removeLimit();

    void checkAndUpdateAllLimits();
}
