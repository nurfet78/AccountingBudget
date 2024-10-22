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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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


    private static final LocalDate CURRENT_DATE = LocalDate.of(2024, 10, 7);
    private static final LocalDate WEEK_LATER = CURRENT_DATE.plusWeeks(1).minusDays(1);
    private static final LocalDate NEXT_MONTH = CURRENT_DATE.plusMonths(1).withDayOfMonth(1).minusDays(1);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("expense-limit.current-date", CURRENT_DATE::toString);
    }

    @BeforeEach
    void setUp() {
        when(expenseLimitRepository.save(any(ExpenseLimit.class))).thenAnswer(invocation -> invocation.getArgument(0));
        System.setProperty("expense-limit.current-date", CURRENT_DATE.toString());
        System.out.println("Текущая дата установлена в setUp: " + CURRENT_DATE);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("expense-limit.current-date");
    }

    @Test
    void testWeeklyLimitReset() {
        ExpenseLimit weeklyLimit = new ExpenseLimit();
        weeklyLimit.setAmount(1000);
        weeklyLimit.setLimitPeriod(ExpenseLimit.LimitPeriod.WEEKLY, CURRENT_DATE.minusWeeks(1));
        when(expenseLimitRepository.findTopByOrderByStartDateDesc()).thenReturn(Optional.of(weeklyLimit));

        expenseLimitService.checkAndResetLimitIfNeeded();

        LocalDate expectedStartDate = CURRENT_DATE;
        LocalDate expectedEndDate = expectedStartDate.plusDays(6);

        LocalDate startDate = CURRENT_DATE.minusWeeks(1);
        LocalDate endDate = CURRENT_DATE.minusDays(1);


        assertEquals(expectedStartDate, weeklyLimit.getStartDate(), "Дата начала должна быть текущей датой");
        assertEquals(expectedEndDate, weeklyLimit.getEndDate(), "Дата окончания должна быть через 6 дней от даты начала");
        verifyLimitResetNotification(1000.0, startDate, endDate, expectedStartDate, ExpenseLimit.LimitPeriod.WEEKLY);
    }

    @Test
    void testWeeklyLimitNoReset() {
        ExpenseLimit weeklyLimit = new ExpenseLimit();
        weeklyLimit.setAmount(1000);
        weeklyLimit.setLimitPeriod(ExpenseLimit.LimitPeriod.WEEKLY, CURRENT_DATE);
        when(expenseLimitRepository.findTopByOrderByStartDateDesc()).thenReturn(Optional.of(weeklyLimit));

        expenseLimitService.checkAndResetLimitIfNeeded();

        assertEquals(CURRENT_DATE, weeklyLimit.getStartDate());
        assertEquals(WEEK_LATER, weeklyLimit.getEndDate());
        verify(mailEventPublisherService, never()).publishMailCreatedEvent(any(SendMessage.class));
    }

    @Test
    void testMonthlyLimitReset() {
        ExpenseLimit monthlyLimit = new ExpenseLimit();
        monthlyLimit.setAmount(5000);
        monthlyLimit.setLimitPeriod(ExpenseLimit.LimitPeriod.MONTHLY, CURRENT_DATE.minusMonths(1));
        when(expenseLimitRepository.findTopByOrderByStartDateDesc()).thenReturn(Optional.of(monthlyLimit));

        expenseLimitService.checkAndResetLimitIfNeeded();

        LocalDate expectedStartDate = CURRENT_DATE;
        LocalDate expectedEndDate = expectedStartDate.plusMonths(1).minusDays(1);

        LocalDate startDate = CURRENT_DATE.minusMonths(1);
        LocalDate endDate = CURRENT_DATE.minusDays(1);

        assertEquals(expectedStartDate, monthlyLimit.getStartDate(), "Дата начала должна быть текущей датой");
        assertEquals(expectedEndDate, monthlyLimit.getEndDate(), "Дата окончания должна быть через месяц минус один день от новой даты начала");
        verifyLimitResetNotification(5000.0, startDate, endDate, expectedStartDate, ExpenseLimit.LimitPeriod.MONTHLY);
    }

    @Test
    void testMonthlyLimitNoReset() {
        ExpenseLimit monthlyLimit = new ExpenseLimit();
        monthlyLimit.setAmount(5000);
        monthlyLimit.setLimitPeriod(ExpenseLimit.LimitPeriod.MONTHLY, CURRENT_DATE.withDayOfMonth(1));
        when(expenseLimitRepository.findTopByOrderByStartDateDesc()).thenReturn(Optional.of(monthlyLimit));

        expenseLimitService.checkAndResetLimitIfNeeded();

        assertEquals(CURRENT_DATE.withDayOfMonth(1), monthlyLimit.getStartDate());
        assertEquals(NEXT_MONTH, monthlyLimit.getEndDate());
        verify(mailEventPublisherService, never()).publishMailCreatedEvent(any(SendMessage.class));
    }

    @Test
    void testLimitResetAtMonthEnd() {
        LocalDate monthEnd = CURRENT_DATE.withDayOfMonth(CURRENT_DATE.lengthOfMonth());
        LocalDate nextMonthStart = monthEnd.plusDays(1);
        System.setProperty("expense-limit.current-date", monthEnd.toString());

        ExpenseLimit monthlyLimit = new ExpenseLimit();
        monthlyLimit.setAmount(5000);
        monthlyLimit.setLimitPeriod(ExpenseLimit.LimitPeriod.MONTHLY, CURRENT_DATE.withDayOfMonth(1));
        when(expenseLimitRepository.findTopByOrderByStartDateDesc()).thenReturn(Optional.of(monthlyLimit));

        expenseLimitService.checkAndResetLimitIfNeeded();

        // Проверяем, что лимит сбрасывается в последний день месяца
        assertEquals(nextMonthStart, monthlyLimit.getStartDate(), "Дата начала должна быть установлена на первый день следующего месяца");
        assertEquals(nextMonthStart.plusMonths(1).minusDays(1), monthlyLimit.getEndDate(), "Дата окончания должна быть последним днем следующего месяца");
        verifyLimitResetNotification(5000.0, CURRENT_DATE.withDayOfMonth(1), monthEnd, nextMonthStart, ExpenseLimit.LimitPeriod.MONTHLY);
    }

    @Test
    void testLimitResetAtNextMonthStart() {
        LocalDate nextMonthStart = CURRENT_DATE.plusMonths(1).withDayOfMonth(1);
        System.setProperty("expense-limit.current-date", nextMonthStart.toString());

        ExpenseLimit monthlyLimit = new ExpenseLimit();
        monthlyLimit.setAmount(5000);
        monthlyLimit.setLimitPeriod(ExpenseLimit.LimitPeriod.MONTHLY, CURRENT_DATE.withDayOfMonth(1));
        when(expenseLimitRepository.findTopByOrderByStartDateDesc()).thenReturn(Optional.of(monthlyLimit));

        expenseLimitService.checkAndResetLimitIfNeeded();

        assertEquals(nextMonthStart, monthlyLimit.getStartDate(), "Дату начала следует обновить до первого дня следующего месяца");
        assertEquals(nextMonthStart.plusMonths(1).minusDays(1), monthlyLimit.getEndDate(), "Дата окончания должна быть последним днем следующего месяца");
        verifyLimitResetNotification(5000.0, CURRENT_DATE.withDayOfMonth(1), nextMonthStart.minusDays(1),
                nextMonthStart, ExpenseLimit.LimitPeriod.MONTHLY);
    }

    private void verifyLimitResetNotification(double expectedAmount, LocalDate startDate, LocalDate endDate,
                                              LocalDate newStartDate, ExpenseLimit.LimitPeriod periodType) {
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mailEventPublisherService).publishMailCreatedEvent(messageCaptor.capture());
        SendMessage capturedMessage = messageCaptor.getValue();

        String periodTypeString = (periodType == ExpenseLimit.LimitPeriod.WEEKLY) ? "Неделя" : "Месяц";

        String expectedMessage = String.format(
                "Ваш лимит расходов в размере %.2f был сброшен. Тип периода: '%s'. Старый период: %s - %s. Новый период начался с %s.",
                expectedAmount,
                periodTypeString,
                startDate, endDate, newStartDate
        );

        System.out.println("Ожидаемое сообщение:   " + expectedMessage);
        System.out.println("Фактическое сообщение: " + capturedMessage.getContent());

        assertEquals(expectedMessage, capturedMessage.getContent(),
                "Сообщение должно точно соответствовать ожидаемому формату");
    }
}
