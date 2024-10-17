package org.nurfet.accountingbudget.controller;

import lombok.RequiredArgsConstructor;
import org.nurfet.accountingbudget.model.Category;
import org.nurfet.accountingbudget.model.Transaction;
import org.nurfet.accountingbudget.service.CategoryService;
import org.nurfet.accountingbudget.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    private final CategoryService categoryService;

    @GetMapping
    public String getTransactions(Model model) {
        model.addAttribute("transactions", transactionService.getAllTransactions());

        return "transactions";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("transaction", new Transaction());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "transaction-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("transaction", transactionService.getTransactionById(id));
        model.addAttribute("categories", categoryService.getAllCategories());

        Transaction t = new Transaction();
        return "transaction-form";
    }

    @PostMapping("/save")
    public String saveTransaction(@ModelAttribute Transaction transaction, @RequestParam Long categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        transaction.setCategory(category);
        transaction.setType(category.getDefaultType());
        transactionService.addTransaction(transaction);
        return "redirect:/transactions";
    }

    @GetMapping("/delete/{id}")
    public String deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return "redirect:/transactions";
    }

    @GetMapping("/by-category/{categoryName}")
    public String getTransactionsByCategory(@PathVariable String categoryName, Model model) {
        List<Transaction> transactions = transactionService.getTransactionsByCategory(categoryName);
        model.addAttribute("transactions", transactions);
        model.addAttribute("categoryName", categoryName);
        return "transactions-by-categoryName";
    }

    @GetMapping("/report")
    public String getTransactionReport(
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null || endDate == null) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "transaction-report";
        }

        List<Transaction> transactions;
        double totalAmount;

        if (categoryName == null || "All".equals(categoryName)) {
            transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        } else {
            transactions = transactionService.getTransactionsByDateRangeAndCategory(startDate, endDate, categoryName);
        }
        totalAmount = transactions.stream().mapToDouble(Transaction::getAmount).sum();

        model.addAttribute("transactions", transactions);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("totalAmount", totalAmount);

        return "transaction-report";
    }

    @GetMapping("/updateType")
    @ResponseBody
    public String updateTransactionType(@RequestParam Long categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        return category.getDefaultType().toString();
    }
}
