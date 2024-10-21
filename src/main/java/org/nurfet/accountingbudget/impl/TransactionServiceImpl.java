package org.nurfet.accountingbudget.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.accountingbudget.dto.ReportDTO;
import org.nurfet.accountingbudget.exception.NotFoundException;
import org.nurfet.accountingbudget.model.Category;
import org.nurfet.accountingbudget.model.ExpenseLimit;
import org.nurfet.accountingbudget.model.TransactionType;
import org.nurfet.accountingbudget.observer.model.SendMessage;
import org.nurfet.accountingbudget.model.Transaction;
import org.nurfet.accountingbudget.repository.CategoryRepository;
import org.nurfet.accountingbudget.repository.TransactionRepository;
import org.nurfet.accountingbudget.observer.service.MailEventPublisherService;
import org.nurfet.accountingbudget.service.ExpenseLimitService;
import org.nurfet.accountingbudget.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.nurfet.accountingbudget.model.TransactionType.EXPENSE;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final CategoryRepository categoryRepository;

    private final MailEventPublisherService mailEventPublisherService;

    private final ExpenseLimitService expenseLimitService;


    @Override
    @Transactional
    public Transaction addTransaction(Transaction transaction) {

        Transaction savedTransaction = transactionRepository.save(transaction);

        ExpenseLimit currentLimit = expenseLimitService.getCurrentLimit();
        if (currentLimit != null && transaction.getType() == TransactionType.EXPENSE) {
            LocalDate startDate = currentLimit.getStartDate();
            LocalDate endDate = LocalDate.now();

            double totalExpenses = transactionRepository.findByDateBetweenAndType(startDate, endDate, TransactionType.EXPENSE)
                    .stream()
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            if (totalExpenses > currentLimit.getAmount()) {
                mailEventPublisherService.publishMailCreatedEvent(SendMessage.create("Превышен лимит расходов! Текущая сумма: " + totalExpenses));
            }
        }

        return savedTransaction;
    }

    @Override
    public List<Transaction> getTransactionsByCategory(String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new NotFoundException(Category.class, null));
        return transactionRepository.findByCategory(category);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Transactional.class, id));
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }

    @Override
    public List<Transaction> getFilteredTransactions(LocalDate startDate, LocalDate endDate, String categoryName, TransactionType type) {
        return transactionRepository.findAll().stream()
                .filter(t -> (startDate == null || !t.getDate().isBefore(startDate))
                        && (endDate == null || !t.getDate().isAfter(endDate))
                        && (categoryName == null || t.getCategory().getName().equals(categoryName))
                        && (type == null || t.getType() == type))
                .collect(Collectors.toList());
    }

    @Override
    public ReportDTO generateDetailedReport(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactionList = getFilteredTransactions(startDate, endDate, null, null);

        Map<TransactionType, Double> totals = calculateTypeTotals(startDate, endDate);
        double totalExpense = totals.getOrDefault(EXPENSE, 0.0);
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long totalDaysWithTransaction = transactionList.stream()
                .map(Transaction::getDate)
                .distinct()
                .count();

        double averageExpensePerDay = totalExpense / totalDays;
        double averageExpensePerDayWithTransaction = totalExpense / totalDaysWithTransaction;

        return new ReportDTO(
                transactionList,
                totals,
                totalDays,
                totalDaysWithTransaction,
                averageExpensePerDay,
                averageExpensePerDayWithTransaction);
    }

    @Override
    public Map<TransactionType, Double> calculateTypeTotals(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findAll().stream()
                .filter(t -> (startDate == null || !t.getDate().isBefore(startDate))
                            && (endDate == null || !t.getDate().isAfter(endDate)))
                .collect(Collectors.groupingBy(Transaction::getType, Collectors.summingDouble(Transaction::getAmount)));
    }

    @Override
    public Map<String, Double> calculateCategoryTotals(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findAll().stream()
                .filter(t -> (startDate == null || !t.getDate().isBefore(startDate))
                        && (endDate == null || !t.getDate().isAfter(endDate)))
                .collect(Collectors.groupingBy(t -> t.getCategory().getName(), Collectors.summingDouble(Transaction::getAmount)));
    }
}
