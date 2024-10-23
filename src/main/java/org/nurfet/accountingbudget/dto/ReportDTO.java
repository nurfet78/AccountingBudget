package org.nurfet.accountingbudget.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nurfet.accountingbudget.model.Transaction;
import org.nurfet.accountingbudget.model.TransactionType;
import org.nurfet.accountingbudget.util.BigDecimalSummaryStatistics;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportDTO {

    private List<Transaction> transactions;

    private BigDecimalSummaryStatistics incomeStats;

    private BigDecimalSummaryStatistics expenseStats;

    private long totalDays;

    private long daysWithTransactions;

    private BigDecimal averageExpensePerDay;

    private BigDecimal averageExpensePerTransactionDay;

    private BigDecimal maxDailyExpense;

    private BigDecimal minDailyExpense;

    private BigDecimal maxSingleIncome;

    private BigDecimal minSingleIncome;
}
