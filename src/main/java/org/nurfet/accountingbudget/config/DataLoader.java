package org.nurfet.accountingbudget.config;

import lombok.RequiredArgsConstructor;
import org.nurfet.accountingbudget.model.Category;
import org.nurfet.accountingbudget.model.Transaction;
import org.nurfet.accountingbudget.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;


    @Override
    public void run(String... args) throws Exception {
        addCategory("Продукты", Transaction.TransactionType.EXPENSE);
        addCategory("Одежда", Transaction.TransactionType.EXPENSE);
        addCategory("Коммунальные платежи", Transaction.TransactionType.EXPENSE);
        addCategory("Зарплата", Transaction.TransactionType.INCOME);
        addCategory("Инвестиции", Transaction.TransactionType.INCOME);
    }

    private void addCategory(String categoryName, Transaction.TransactionType defaultType) {
        categoryRepository.findByName(categoryName).orElseGet(() -> {
            Category category = new Category(categoryName, defaultType);
            return categoryRepository.save(category);
        });
    }
}
