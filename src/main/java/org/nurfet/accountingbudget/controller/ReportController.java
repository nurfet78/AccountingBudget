package org.nurfet.accountingbudget.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.accountingbudget.dto.ReportDTO;
import org.nurfet.accountingbudget.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;


@Controller
@RequestMapping("/report")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final TransactionService transactionService;


    @GetMapping
    public String showReport(Model model) {

        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();

        ReportDTO report = transactionService.generateDetailedReport(startDate, endDate);

        Map<String, BigDecimal> res = transactionService.calculateCategoryTotals(startDate, endDate);


        model.addAttribute("report", report);
        model.addAttribute("categoryTotals", transactionService.calculateCategoryTotals(startDate, endDate));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);


        return "report";
    }
}
