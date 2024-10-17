package org.nurfet.accountingbudget.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nurfet.accountingbudget.model.Transaction;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ReportDTO {

    private double totalIncome;

    private double totalExpense;

    private List<Transaction> transactions;

    private Map<String, Double> categoryTotals;
}
