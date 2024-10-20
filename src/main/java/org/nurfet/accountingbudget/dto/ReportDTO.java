package org.nurfet.accountingbudget.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nurfet.accountingbudget.model.Transaction;
import org.nurfet.accountingbudget.model.TransactionType;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportDTO {

    private List<Transaction> transactions;

    private Map<TransactionType, Double> typeTotals;

    private long totalDays;

    private long daysWithTransactions;

    private double averageExpensePerDay;

    private double averageExpensePerTransactionDay;
}
