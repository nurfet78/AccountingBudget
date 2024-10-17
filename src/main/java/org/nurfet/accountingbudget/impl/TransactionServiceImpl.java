package org.nurfet.accountingbudget.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.accountingbudget.dto.ReportDTO;
import org.nurfet.accountingbudget.exception.NotFoundException;
import org.nurfet.accountingbudget.model.Category;
import org.nurfet.accountingbudget.observer.model.SendMessage;
import org.nurfet.accountingbudget.model.Transaction;
import org.nurfet.accountingbudget.repository.CategoryRepository;
import org.nurfet.accountingbudget.repository.TransactionRepository;
import org.nurfet.accountingbudget.observer.service.MailEventPublisherService;
import org.nurfet.accountingbudget.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final CategoryRepository categoryRepository;

    private final MailEventPublisherService mailEventPublisherService;

    private static final double EXPENSE_LIMIT = 50000.0;


    @Override
    @Transactional
    public Transaction addTransaction(Transaction transaction) {

        Transaction savedTransaction = transactionRepository.save(transaction);

        double totalExpenses = calculateTotalExpenses();
        if (totalExpenses > EXPENSE_LIMIT) {
            Optional<SendMessage> messageOpt = SendMessage.create("Превышен лимит расходов! Текущая сумма: " + totalExpenses);
            messageOpt.ifPresentOrElse(
                    mailEventPublisherService::publishMailCreatedEvent,
                    () -> log.error("Сообщение не создано: контент отсутствует")
            );
        }

        return savedTransaction;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findAll().stream()
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> getTransactionsByCategory(String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new NotFoundException(Category.class, null));
        return transactionRepository.findByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByDateRangeAndCategory(LocalDate startDate, LocalDate endDate, String categoryName) {
        Category category = categoryRepository.findByName(categoryName).orElseThrow(() -> new NotFoundException(Category.class, null));
        return getTransactionsByDateRange(startDate, endDate).stream()
                .filter(t -> t.getCategory().equals(category))
                .collect(Collectors.toList());
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
    public List<Transaction> getFilteredTransactions(LocalDate startDate, LocalDate endDate, String categoryName, Transaction.TransactionType type) {
        return transactionRepository.findAll().stream()
                .filter(t -> (startDate == null || !t.getDate().isBefore(startDate))
                        && (endDate == null || !t.getDate().isAfter(endDate))
                        && (categoryName == null || t.getCategory().getName().equals(categoryName))
                        && (type == null || t.getType() == type))
                .collect(Collectors.toList());
    }

    @Override
    public ReportDTO generateDetailedReport(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = getFilteredTransactions(startDate, endDate, null, null);
        double totalIncome = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
        double totalExpense = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();

        Map<String, Double> categoryTotals = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getCategory().getName(),
                        Collectors.summingDouble(Transaction::getAmount)));

        return new ReportDTO(totalIncome, totalExpense, transactions, categoryTotals);
    }

    private double calculateTotalExpenses() {
        return transactionRepository.findAll().stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }


}
