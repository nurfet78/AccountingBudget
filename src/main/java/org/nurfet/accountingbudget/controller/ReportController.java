package org.nurfet.accountingbudget.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.accountingbudget.dto.ReportDTO;
import org.nurfet.accountingbudget.model.Transaction;
import org.nurfet.accountingbudget.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/report")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final TransactionService transactionService;

    @GetMapping
    public String showReport(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                             Model model) {
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        ReportDTO report = transactionService.generateDetailedReport(startDate, endDate);
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double totalExpense = Math.abs(report.getTotalExpense());

        long daysWithTransactions = report.getTransactions().stream()
                .map(Transaction::getDate)
                .distinct()
                .count();

        double averageExpensePerDay = totalExpense / totalDays;
        double averageExpensePerTransactionDay = daysWithTransactions > 0 ? totalExpense / daysWithTransactions : 0;

        // Подготовка данных для графика
        List<String> dates = report.getTransactions().stream()
                .map(t -> t.getDate().format(DateTimeFormatter.ISO_DATE))
                .collect(Collectors.toList());
        List<Double> amounts = report.getTransactions().stream()
                .map(Transaction::getAmount)
                .collect(Collectors.toList());


        model.addAttribute("chartDates", dates);
        model.addAttribute("chartAmounts", amounts);
        model.addAttribute("report", report);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("totalDays", totalDays);
        model.addAttribute("daysWithTransactions", daysWithTransactions);
        model.addAttribute("averageExpensePerDay", averageExpensePerDay);
        model.addAttribute("averageExpensePerTransactionDay", averageExpensePerTransactionDay);
        model.addAttribute("transactions", report.getTransactions());
        return "report";
    }
}
