package org.nurfet.accountingbudget.config;

import lombok.RequiredArgsConstructor;
import org.nurfet.accountingbudget.model.Category;
import org.nurfet.accountingbudget.model.TransactionType;
import org.nurfet.accountingbudget.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import static org.nurfet.accountingbudget.model.TransactionType.EXPENSE;
import static org.nurfet.accountingbudget.model.TransactionType.INCOME;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;


    @Override
    public void run(String... args) throws Exception {
        addCategory("Продукты", EXPENSE);
        addCategory("Непродовольственные товары", EXPENSE);
        addCategory("Бытовая химия", EXPENSE);
        addCategory("Средства личной гигиены", EXPENSE);
        addCategory("Ювелирные изделия", EXPENSE);
        addCategory("Спортивные товары", EXPENSE);
        addCategory("Одежда", EXPENSE);
        addCategory("Коммунальные платежи", EXPENSE);
        addCategory("Зарплата", INCOME);
        addCategory("Инвестиции", INCOME);
        addCategory("Подработка", INCOME);
    }

    private void addCategory(String categoryName, TransactionType defaultType) {
        categoryRepository.findByName(categoryName).orElseGet(() -> {
            Category category = new Category(categoryName, defaultType);
            return categoryRepository.save(category);
        });
    }
}
