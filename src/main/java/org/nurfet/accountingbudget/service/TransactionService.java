package org.nurfet.accountingbudget.service;

import org.nurfet.accountingbudget.dto.ReportDTO;
import org.nurfet.accountingbudget.model.Transaction;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    Transaction addTransaction(Transaction transaction);

    List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate);

    List<Transaction> getTransactionsByCategory(String categoryName);

    List<Transaction> getTransactionsByDateRangeAndCategory(LocalDate startDate, LocalDate endDate, String categoryName);


    List<Transaction> getAllTransactions();

    Transaction getTransactionById(Long id);

    void deleteTransaction(Long id);

    List<Transaction> getFilteredTransactions(LocalDate startDate, LocalDate endDate,
                                              String categoryName, Transaction.TransactionType type);

    ReportDTO generateBasicReport(LocalDate startDate, LocalDate endDate);
}
