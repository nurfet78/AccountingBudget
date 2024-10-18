package org.nurfet.accountingbudget.impl;

import lombok.RequiredArgsConstructor;
import org.nurfet.accountingbudget.dto.ReportDTO;
import org.nurfet.accountingbudget.service.ReportService;
import org.nurfet.accountingbudget.service.TransactionService;
import org.springframework.stereotype.Service;
import org.nurfet.accountingbudget.model.Transaction;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TransactionService transactionService;


    @Override
    public ReportDTO generateDetailedReport(LocalDate startDate, LocalDate endDate) {
        ReportDTO report = transactionService.generateBasicReport(startDate, endDate);


        // Возвращаем количество уникальных дат в которые были проведены транзакции
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long daysWithTransactions = report.getTransactions().stream()
                .map(Transaction::getDate)
                .distinct()
                .count();

        double totalExpense = Math.abs(report.getTotalExpense());

        //Среднее значение трат за каждый день отчётного периода
        double averageExpensePerDay = totalExpense / totalDays;

        //Среднее значение трат за каждый день с транзакциями
        double averageExpensePerTransactionDay = daysWithTransactions > 0 ? totalExpense / daysWithTransactions : 0;

        report.setTotalDays(totalDays);
        report.setDaysWithTransactions(daysWithTransactions);
        report.setAverageExpensePerDay(averageExpensePerDay);
        report.setAverageExpensePerTransactionDay(averageExpensePerTransactionDay);

        return report;
    }
}
