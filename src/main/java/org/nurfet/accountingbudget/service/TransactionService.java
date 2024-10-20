package org.nurfet.accountingbudget.service;

import org.nurfet.accountingbudget.dto.ReportDTO;
import org.nurfet.accountingbudget.model.Transaction;
import org.nurfet.accountingbudget.model.TransactionType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TransactionService {

    Transaction addTransaction(Transaction transaction);

    List<Transaction> getTransactionsByCategory(String categoryName);

    List<Transaction> getAllTransactions();

    Transaction getTransactionById(Long id);

    void deleteTransaction(Long id);

    List<Transaction> getFilteredTransactions(LocalDate startDate, LocalDate endDate,
                                              String categoryName, TransactionType type);

    ReportDTO generateDetailedReport(LocalDate startDate, LocalDate endDate);

    Map<TransactionType, Double> calculateTypeTotals();

    Map<String, Double> calculateCategoryTotals();
}
