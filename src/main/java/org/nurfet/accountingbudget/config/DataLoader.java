package org.nurfet.accountingbudget.config;

import lombok.RequiredArgsConstructor;
import org.nurfet.accountingbudget.model.Category;
import org.nurfet.accountingbudget.model.Transaction;
import org.nurfet.accountingbudget.model.TransactionType;
import org.nurfet.accountingbudget.repository.CategoryRepository;
import org.nurfet.accountingbudget.repository.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.nurfet.accountingbudget.model.TransactionType.EXPENSE;
import static org.nurfet.accountingbudget.model.TransactionType.INCOME;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class DataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    private final TransactionRepository transactionRepository;


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

        addTestTransactions();
    }

    private void addTestTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        // Доходы
        transactions.add(new Transaction(LocalDate.of(2024, 10, 5), new BigDecimal("150000"), INCOME, findCategory("Зарплата"), "Зарплата за октябрь"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 10), new BigDecimal("25000"), INCOME, findCategory("Подработка"), "Фриланс проект"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 15), new BigDecimal("5000"), INCOME, findCategory("Инвестиции"), "Дивиденды"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 20), new BigDecimal("30000"), INCOME, findCategory("Подработка"), "Консультация"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 25), new BigDecimal("7000"), INCOME, findCategory("Инвестиции"), "Проценты по вкладу"));

        // Расходы
        transactions.add(new Transaction(LocalDate.of(2024, 10, 1), new BigDecimal("5000"), EXPENSE, findCategory("Продукты"), "Еженедельная закупка"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 1), new BigDecimal("2000"), EXPENSE, findCategory("Бытовая химия"), "Стиральный порошок и мыло"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 2), new BigDecimal("3500"), EXPENSE, findCategory("Одежда"), "Новая рубашка"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 2), new BigDecimal("1500"), EXPENSE, findCategory("Средства личной гигиены"), "Шампунь и зубная паста"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 3), new BigDecimal("6000"), EXPENSE, findCategory("Продукты"), "Закупка на неделю"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 3), new BigDecimal("15000"), EXPENSE, findCategory("Коммунальные платежи"), "Счет за электричество и воду"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 4), new BigDecimal("4000"), EXPENSE, findCategory("Непродовольственные товары"), "Новый чайник"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 4), new BigDecimal("7000"), EXPENSE, findCategory("Спортивные товары"), "Новые кроссовки для бега"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 5), new BigDecimal("5500"), EXPENSE, findCategory("Продукты"), "Закупка на выходные"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 6), new BigDecimal("30000"), EXPENSE, findCategory("Ювелирные изделия"), "Подарок на годовщину"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 7), new BigDecimal("2500"), EXPENSE, findCategory("Бытовая химия"), "Средства для уборки"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 8), new BigDecimal("4500"), EXPENSE, findCategory("Одежда"), "Новые джинсы"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 9), new BigDecimal("6000"), EXPENSE, findCategory("Продукты"), "Закупка на неделю"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 10), new BigDecimal("1800"), EXPENSE, findCategory("Средства личной гигиены"), "Крем для лица"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 11), new BigDecimal("3000"), EXPENSE, findCategory("Непродовольственные товары"), "Новая настольная лампа"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 12), new BigDecimal("5000"), EXPENSE, findCategory("Продукты"), "Закупка на выходные"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 13), new BigDecimal("12000"), EXPENSE, findCategory("Коммунальные платежи"), "Счет за интернет и телефон"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 14), new BigDecimal("8000"), EXPENSE, findCategory("Одежда"), "Осенняя куртка"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 15), new BigDecimal("2000"), EXPENSE, findCategory("Бытовая химия"), "Чистящие средства"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 16), new BigDecimal("5500"), EXPENSE, findCategory("Продукты"), "Еженедельная закупка"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 17), new BigDecimal("3500"), EXPENSE, findCategory("Спортивные товары"), "Гантели"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 18), new BigDecimal("2500"), EXPENSE, findCategory("Средства личной гигиены"), "Косметика"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 19), new BigDecimal("6000"), EXPENSE, findCategory("Продукты"), "Закупка на выходные"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 20), new BigDecimal("4000"), EXPENSE, findCategory("Непродовольственные товары"), "Постельное белье"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 21), new BigDecimal("3000"), EXPENSE, findCategory("Одежда"), "Новая футболка"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 22), new BigDecimal("5000"), EXPENSE, findCategory("Продукты"), "Еженедельная закупка"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 23), new BigDecimal("2000"), EXPENSE, findCategory("Бытовая химия"), "Средства для мытья посуды"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 24), new BigDecimal("7000"), EXPENSE, findCategory("Коммунальные платежи"), "Счет за газ"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 25), new BigDecimal("6500"), EXPENSE, findCategory("Продукты"), "Закупка на выходные"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 1), new BigDecimal("1000"), EXPENSE, findCategory("Средства личной гигиены"), "Зубная щетка и паста"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 2), new BigDecimal("2500"), EXPENSE, findCategory("Непродовольственные товары"), "Книги"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 3), new BigDecimal("3000"), EXPENSE, findCategory("Одежда"), "Носки и нижнее белье"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 4), new BigDecimal("1500"), EXPENSE, findCategory("Продукты"), "Фрукты и овощи"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 5), new BigDecimal("2000"), EXPENSE, findCategory("Бытовая химия"), "Освежитель воздуха"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 6), new BigDecimal("4000"), EXPENSE, findCategory("Спортивные товары"), "Спортивная форма"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 7), new BigDecimal("1800"), EXPENSE, findCategory("Продукты"), "Молочные продукты"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 8), new BigDecimal("3500"), EXPENSE, findCategory("Непродовольственные товары"), "Канцелярские товары"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 9), new BigDecimal("2200"), EXPENSE, findCategory("Средства личной гигиены"), "Бритвенные принадлежности"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 10), new BigDecimal("5000"), EXPENSE, findCategory("Одежда"), "Обувь"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 11), new BigDecimal("1700"), EXPENSE, findCategory("Продукты"), "Хлебобулочные изделия"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 12), new BigDecimal("2800"), EXPENSE, findCategory("Бытовая химия"), "Средства для мытья полов"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 13), new BigDecimal("3200"), EXPENSE, findCategory("Непродовольственные товары"), "Посуда"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 14), new BigDecimal("1900"), EXPENSE, findCategory("Продукты"), "Мясные продукты"));
        transactions.add(new Transaction(LocalDate.of(2024, 10, 15), new BigDecimal("2600"), EXPENSE, findCategory("Средства личной гигиены"), "Средства для ухода за волосами"));

        transactionRepository.saveAll(transactions);
    }

    private void addCategory(String categoryName, TransactionType defaultType) {
        categoryRepository.findByName(categoryName).orElseGet(() -> {
            Category category = new Category(categoryName, defaultType);
            return categoryRepository.save(category);
        });
    }

    private Category findCategory(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Category not found: " + name));
    }
}
