package org.nurfet.accountingbudget.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.nurfet.accountingbudget.model.ExpenseLimit;
import org.nurfet.accountingbudget.observer.model.SendMessage;
import org.nurfet.accountingbudget.observer.service.MailEventPublisherService;
import org.nurfet.accountingbudget.repository.ExpenseLimitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ExpenseLimitServiceTest {

    @Autowired
    private ExpenseLimitService expenseLimitService;

    @MockBean
    private ExpenseLimitRepository expenseLimitRepository;

    @MockBean
    private MailEventPublisherService mailEventPublisherService;

    private static final LocalDate CURRENT_DATE = LocalDate.of(2024, 10, 21);
    private static final LocalDate WEEK_LATER = CURRENT_DATE.plusWeeks(1);
    private static final LocalDate NEXT_MONTH = CURRENT_DATE.plusMonths(1);

    private List<ExpenseLimit> limits;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("expense-limit.current-date", CURRENT_DATE::toString);
    }

    @BeforeEach
    void setUp() {
        limits = new ArrayList<>();
        when(expenseLimitRepository.findAll()).thenReturn(limits);
        when(expenseLimitRepository.save(any(ExpenseLimit.class))).thenAnswer(invocation -> {
            return invocation.<ExpenseLimit>getArgument(0);
        });

        // Устанавливаем текущую дату для всех тестов
        System.setProperty("expense-limit.current-date", CURRENT_DATE.toString());
        System.out.println("Текущая дата установлена в setUp: " + CURRENT_DATE);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("expense-limit.current-date");
    }

    @Test
    void testWeeklyLimitReset() {
        // Устанавливаем лимит на неделю назад
        ExpenseLimit weeklyLimit = new ExpenseLimit();
        weeklyLimit.setAmount(1000);
        weeklyLimit.setPeriod(ExpenseLimit.LimitPeriod.WEEKLY);
        weeklyLimit.setStartDate(CURRENT_DATE.minusWeeks(1));
        weeklyLimit.updateNextResetDate();
        limits.add(weeklyLimit);

        expenseLimitService.checkAndUpdateAllLimits();

        assertEquals(CURRENT_DATE, weeklyLimit.getStartDate());
        assertEquals(CURRENT_DATE.plusWeeks(1), weeklyLimit.getNextResetDate());

        verifyLimitResetNotification(1000);
    }

    @Test
    void testWeeklyLimitNoReset() {
        // Устанавливаем лимит на текущую дату
        ExpenseLimit weeklyLimit = new ExpenseLimit();
        weeklyLimit.setAmount(1000);
        weeklyLimit.setPeriod(ExpenseLimit.LimitPeriod.WEEKLY);
        weeklyLimit.setStartDate(CURRENT_DATE);
        weeklyLimit.updateNextResetDate();
        limits.add(weeklyLimit);

        // Устанавливаем текущую дату
        System.setProperty("expense-limit.current-date", CURRENT_DATE.toString());
        expenseLimitService.checkAndUpdateAllLimits();

        assertEquals(CURRENT_DATE, weeklyLimit.getStartDate());
        assertEquals(WEEK_LATER, weeklyLimit.getNextResetDate());

        verify(mailEventPublisherService, never()).publishMailCreatedEvent(any(SendMessage.class));
    }

    @Test
    void testMonthlyLimitReset() {
        // Устанавливаем лимит на месяц назад
        ExpenseLimit monthlyLimit = new ExpenseLimit();
        monthlyLimit.setAmount(5000);
        monthlyLimit.setPeriod(ExpenseLimit.LimitPeriod.MONTHLY);
        monthlyLimit.setStartDate(CURRENT_DATE.minusMonths(1));
        monthlyLimit.updateNextResetDate();
        limits.add(monthlyLimit);

        expenseLimitService.checkAndUpdateAllLimits();

        assertEquals(CURRENT_DATE, monthlyLimit.getStartDate());
        assertEquals(CURRENT_DATE.plusMonths(1).withDayOfMonth(1), monthlyLimit.getNextResetDate());

        verifyLimitResetNotification(5000);
    }

    @Test
    void testMonthlyLimitNoReset() {
        // Устанавливаем лимит на начало текущего месяца
        ExpenseLimit monthlyLimit = new ExpenseLimit();
        monthlyLimit.setAmount(5000);
        monthlyLimit.setPeriod(ExpenseLimit.LimitPeriod.MONTHLY);
        monthlyLimit.setStartDate(CURRENT_DATE.withDayOfMonth(1));
        monthlyLimit.updateNextResetDate();
        limits.add(monthlyLimit);

        // Устанавливаем текущую дату
        System.setProperty("expense-limit.current-date", CURRENT_DATE.toString());
        expenseLimitService.checkAndUpdateAllLimits();

        assertEquals(CURRENT_DATE.withDayOfMonth(1), monthlyLimit.getStartDate());
        assertEquals(NEXT_MONTH.withDayOfMonth(1), monthlyLimit.getNextResetDate());

        verify(mailEventPublisherService, never()).publishMailCreatedEvent(any(SendMessage.class));
    }

    @Test
    void testLimitResetAtMonthEnd() {
        // Меняем текущую дату на конец месяца
        LocalDate monthEnd = CURRENT_DATE.withDayOfMonth(CURRENT_DATE.lengthOfMonth());
        System.setProperty("expense-limit.current-date", monthEnd.toString());

        // Устанавливаем лимит на начало текущего месяца
        ExpenseLimit monthlyLimit = new ExpenseLimit();
        monthlyLimit.setAmount(5000);
        monthlyLimit.setPeriod(ExpenseLimit.LimitPeriod.MONTHLY);
        monthlyLimit.setStartDate(monthEnd.withDayOfMonth(1));
        monthlyLimit.updateNextResetDate();
        limits.add(monthlyLimit);

        expenseLimitService.checkAndUpdateAllLimits();

        assertEquals(monthEnd, monthlyLimit.getStartDate(), "Дату начала следует обновить до конца месяца");
        assertEquals(monthEnd.plusMonths(1).withDayOfMonth(1), monthlyLimit.getNextResetDate(),
                "Следующая дата сброса должна идти первым числом следующего месяца");

        verifyLimitResetNotification(5000);

        // Очищаем свойство после теста
        System.clearProperty("expense-limit.current-date");
    }

    @Test
    void testLimitResetAtNextMonthStart() {
        // Меняем текущую дату на начало следующего месяца
        LocalDate nextMonthStart = NEXT_MONTH.withDayOfMonth(1);
        System.setProperty("expense-limit.current-date", nextMonthStart.toString());

        // Устанавливаем лимит на текущий месяц
        ExpenseLimit monthlyLimit = new ExpenseLimit();
        monthlyLimit.setAmount(5000);
        monthlyLimit.setPeriod(ExpenseLimit.LimitPeriod.MONTHLY);
        monthlyLimit.setStartDate(CURRENT_DATE.withDayOfMonth(1));
        monthlyLimit.updateNextResetDate();
        limits.add(monthlyLimit);

        expenseLimitService.checkAndUpdateAllLimits();

        assertEquals(nextMonthStart, monthlyLimit.getStartDate(), "Дату начала следует обновить до начала следующего месяца");
        assertEquals(nextMonthStart.plusMonths(1).withDayOfMonth(1), monthlyLimit.getNextResetDate(),
                "Следующая дата сброса должна приходиться на первый день месяца, следующего за следующим");

        verifyLimitResetNotification(5000);

        // Очищаем свойство после теста
        System.clearProperty("expense-limit.current-date");
    }

    private void verifyLimitResetNotification(double expectedAmount) {
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mailEventPublisherService).publishMailCreatedEvent(messageCaptor.capture());
        SendMessage capturedMessage = messageCaptor.getValue();

        System.out.println("Содержание полученного сообщения: " + capturedMessage.getContent());

        assertTrue(capturedMessage.getContent().contains("лимит расходов"),
                "Сообщение должно содержать фразу 'лимит расходов'");
        assertTrue(capturedMessage.getContent().contains("был сброшен"),
                "Сообщение должно содержать фразу 'был сброшен'");

// Проверяем сумму с учетом форматирования
        String formattedAmount = String.format("%.2f", expectedAmount).replace(".", ",");
        assertTrue(capturedMessage.getContent().contains(formattedAmount),
                "Сообщение должно содержать ожидаемую сумму: " + formattedAmount);

        assertTrue(capturedMessage.getContent().contains("Старый период"),
                "Сообщение должно содержать фразу 'Старый период'");
        assertTrue(capturedMessage.getContent().contains("Новый период"),
                "Сообщение должно содержать фразу 'Новый период'");
    }
}
