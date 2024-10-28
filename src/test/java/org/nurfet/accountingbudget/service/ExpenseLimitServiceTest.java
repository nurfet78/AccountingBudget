package org.nurfet.accountingbudget.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.nurfet.accountingbudget.model.ExpenseLimit;
import org.nurfet.accountingbudget.observer.model.SendMessage;
import org.nurfet.accountingbudget.observer.service.MailEventPublisherService;
import org.nurfet.accountingbudget.repository.ExpenseLimitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Slf4j
public class ExpenseLimitServiceTest {

    @Autowired
    private ExpenseLimitService expenseLimitService;

    @Autowired
    private ExpenseLimitRepository expenseLimitRepository;

    @MockBean
    private MailEventPublisherService mailEventPublisherService;

    private static final LocalDate CURRENT_DATE = LocalDate.of(2024, 10, 27);
    private static final LocalDate WEEK_LATER = CURRENT_DATE.plusWeeks(1).minusDays(1);
    private static final LocalDate NEXT_MONTH = CURRENT_DATE.plusMonths(1).withDayOfMonth(1).minusDays(1);

    @BeforeEach
    void setUp() {
        System.setProperty("expense-limit.current-date", CURRENT_DATE.toString());
        System.out.println("Текущая дата установлена в setUp: " + CURRENT_DATE);
        expenseLimitRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("expense-limit.current-date");
    }

    @Test
    void testWeeklyLimitReset() {
        printTestInfo("testWeeklyLimitReset", "Начальная дата", CURRENT_DATE, getCurrentDate());

        ExpenseLimit weeklyLimit = createAndSaveLimit(new BigDecimal("1000.00"), ExpenseLimit.LimitPeriod.WEEKLY, CURRENT_DATE.minusWeeks(1), true);

        printTestInfo("testWeeklyLimitReset", "Создан недельный лимит",
                String.format("startDate: %s, endDate: %s", CURRENT_DATE.minusWeeks(1), CURRENT_DATE.minusDays(1)),
                String.format("startDate: %s, endDate: %s", weeklyLimit.getStartDate(), weeklyLimit.getEndDate()));

        expenseLimitService.checkAndResetLimitIfNeeded();

        ExpenseLimit updatedLimit = expenseLimitRepository.findById(weeklyLimit.getId()).orElseThrow();

        printTestInfo("testWeeklyLimitReset", "Обновленный лимит",
                String.format("startDate: %s, endDate: %s", CURRENT_DATE, CURRENT_DATE.plusDays(6)),
                String.format("startDate: %s, endDate: %s", updatedLimit.getStartDate(), updatedLimit.getEndDate()));

        assertEquals(CURRENT_DATE, updatedLimit.getStartDate(), "Дата начала должна быть текущей датой");
        assertEquals(CURRENT_DATE.plusDays(6), updatedLimit.getEndDate(), "Дата окончания должна быть через 6 дней от новой даты начала");
        verifyRenewalNotification(updatedLimit, CURRENT_DATE.minusWeeks(1), CURRENT_DATE.minusDays(1), CURRENT_DATE);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mailEventPublisherService).publishMailCreatedEvent(messageCaptor.capture());
        printTestInfo("testWeeklyLimitReset", "Уведомление",
                String.format("Ваш лимит расходов в размере %.2f был автоматически продлен...", updatedLimit.getAmount()),
                messageCaptor.getValue().getContent());
    }

    @Test
    void testWeeklyLimitNoReset() {
        printTestInfo("testWeeklyLimitNoReset", "Начальная дата", CURRENT_DATE, getCurrentDate());

        ExpenseLimit limit = createAndSaveLimit(new BigDecimal("1000.00"), ExpenseLimit.LimitPeriod.WEEKLY, CURRENT_DATE, true);

        printTestInfo("testWeeklyLimitNoReset", "Создан недельный лимит",
                String.format("startDate: %s, endDate: %s", CURRENT_DATE, WEEK_LATER),
                String.format("startDate: %s, endDate: %s", limit.getStartDate(), limit.getEndDate()));

        expenseLimitService.checkAndResetLimitIfNeeded();

        ExpenseLimit currentLimit = expenseLimitService.getCurrentLimit();
        assertEquals(CURRENT_DATE, currentLimit.getStartDate());
        assertEquals(WEEK_LATER, currentLimit.getEndDate());
        verify(mailEventPublisherService, never()).publishMailCreatedEvent(any(SendMessage.class));

        printTestInfo("testWeeklyLimitNoReset", "Текущий лимит",
                String.format("startDate: %s, endDate: %s", CURRENT_DATE, WEEK_LATER),
                String.format("startDate: %s, endDate: %s", currentLimit.getStartDate(), currentLimit.getEndDate()));

        printTestInfo("testWeeklyLimitNoReset", "Проверка отсутствия уведомлений",
                "Уведомления не должны отправляться",
                "verify(mailEventPublisherService, never()).publishMailCreatedEvent()");
    }

    @Test
    void testMonthlyLimitReset() {
        printTestInfo("testMonthlyLimitReset", "Начальная дата", CURRENT_DATE, getCurrentDate());

        ExpenseLimit monthlyLimit = createAndSaveLimit(new BigDecimal("5000.00"), ExpenseLimit.LimitPeriod.MONTHLY,
                CURRENT_DATE.minusMonths(1), true);

        printTestInfo("testMonthlyLimitReset", "Создан месячный лимит",
                String.format("startDate: %s, endDate: %s", CURRENT_DATE.minusMonths(1), CURRENT_DATE.minusDays(1)),
                String.format("startDate: %s, endDate: %s", monthlyLimit.getStartDate(), monthlyLimit.getEndDate()));

        expenseLimitService.checkAndResetLimitIfNeeded();

        ExpenseLimit updatedLimit = expenseLimitRepository.findById(monthlyLimit.getId()).orElseThrow();

        printTestInfo("testMonthlyLimitReset", "Обновленный лимит",
                String.format("startDate: %s, endDate: %s", CURRENT_DATE, CURRENT_DATE.plusMonths(1).minusDays(1)),
                String.format("startDate: %s, endDate: %s", updatedLimit.getStartDate(), updatedLimit.getEndDate()));

        assertEquals(CURRENT_DATE, updatedLimit.getStartDate(), "Дата начала должна быть текущей датой");
        assertEquals(CURRENT_DATE.plusMonths(1).minusDays(1), updatedLimit.getEndDate(),
                "Дата окончания должна быть через месяц минус один день от новой даты начала");
        verifyRenewalNotification(updatedLimit, CURRENT_DATE.minusMonths(1), CURRENT_DATE.minusDays(1), CURRENT_DATE);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mailEventPublisherService).publishMailCreatedEvent(messageCaptor.capture());
        printTestInfo("testMonthlyLimitReset", "Уведомление",
                String.format("Ваш лимит расходов в размере %.2f был автоматически продлен...", updatedLimit.getAmount()),
                messageCaptor.getValue().getContent());
    }

    @Test
    void testMonthlyLimitNoReset() {
        printTestInfo("testMonthlyLimitNoReset", "Начальная дата", CURRENT_DATE, getCurrentDate());

        ExpenseLimit limit = createAndSaveLimit(new BigDecimal("5000.00"), ExpenseLimit.LimitPeriod.MONTHLY, CURRENT_DATE.withDayOfMonth(1), true);

        printTestInfo("testMonthlyLimitNoReset", "Создан месячный лимит",
                String.format("startDate: %s, endDate: %s", CURRENT_DATE.withDayOfMonth(1), NEXT_MONTH),
                String.format("startDate: %s, endDate: %s", limit.getStartDate(), limit.getEndDate()));

        expenseLimitService.checkAndResetLimitIfNeeded();

        ExpenseLimit currentLimit = expenseLimitService.getCurrentLimit();
        assertEquals(CURRENT_DATE.withDayOfMonth(1), currentLimit.getStartDate());
        assertEquals(NEXT_MONTH, currentLimit.getEndDate());
        verify(mailEventPublisherService, never()).publishMailCreatedEvent(any(SendMessage.class));

        printTestInfo("testMonthlyLimitNoReset", "Текущий лимит",
                String.format("startDate: %s, endDate: %s", CURRENT_DATE.withDayOfMonth(1), NEXT_MONTH),
                String.format("startDate: %s, endDate: %s", currentLimit.getStartDate(), currentLimit.getEndDate()));

        printTestInfo("testMonthlyLimitNoReset", "Проверка отсутствия уведомлений",
                "Уведомления не должны отправляться",
                "verify(mailEventPublisherService, never()).publishMailCreatedEvent()");
    }

    @Test
    void testLimitTransitionWithFutureLimit() {
        LocalDate startDate = LocalDate.of(2024, 10, 27);
        System.setProperty("expense-limit.current-date", startDate.toString());

        printTestInfo("testLimitTransitionWithFutureLimit", "Начальная дата", startDate, getCurrentDate());

        // Создаем первый лимит с автопродлением
        ExpenseLimit firstLimit = createAndSaveLimit(new BigDecimal("1000.00"), ExpenseLimit.LimitPeriod.WEEKLY, startDate, true);
        printTestInfo("testLimitTransitionWithFutureLimit", "Создан первый лимит",
                String.format("startDate: %s, endDate: %s", startDate, startDate.plusDays(6)),
                String.format("startDate: %s, endDate: %s", firstLimit.getStartDate(), firstLimit.getEndDate()));

        // Проверяем сценарий с будущим лимитом
        expenseLimitService.setFutureLimitAmount(new BigDecimal("500.00"), ExpenseLimit.LimitPeriod.WEEKLY, false);

        ExpenseLimit futureLimit = expenseLimitService.getFutureLimit();

        printTestInfo("testLimitTransitionWithFutureLimit", "Установлен будущий лимит",
                "amount: 500.00, period: WEEKLY, autoRenew: false",
                String.format("amount: %.2f, period: %s, autoRenew: %s",
                        futureLimit.getAmount(), futureLimit.getPeriod(), futureLimit.isAutoRenew()));

        // Симулируем прохождение времени до дня окончания первого лимита
        LocalDate endDate = startDate.plusDays(7); // 2024-11-03
        System.setProperty("expense-limit.current-date", endDate.toString());

        printTestInfo("testLimitTransitionWithFutureLimit", "Новая дата", endDate, getCurrentDate());

        expenseLimitService.checkAndResetLimitIfNeeded();

        ExpenseLimit newCurrentLimit = expenseLimitService.getCurrentLimit();
        printTestInfo("testLimitTransitionWithFutureLimit", "Новый текущий лимит",
                String.format("amount: 500.00, startDate: %s, endDate: %s",
                        endDate, endDate.plusDays(6)),
                String.format("amount: %.2f, startDate: %s, endDate: %s",
                        newCurrentLimit.getAmount(), newCurrentLimit.getStartDate(), newCurrentLimit.getEndDate()));

        assertNotNull(newCurrentLimit, "Новый текущий лимит не должен быть null");
        assertEquals(new BigDecimal("500.00"), newCurrentLimit.getAmount(), "Сумма нового текущего лимита должна быть 500.00");
        assertEquals(endDate, newCurrentLimit.getStartDate(), "Новый текущий лимит должен начинаться с даты окончания предыдущего");

        // Проверяем отправку уведомления о новом лимите
        verifyLimitNotification(newCurrentLimit, true);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mailEventPublisherService).publishMailCreatedEvent(messageCaptor.capture());
        printTestInfo("testLimitTransitionWithFutureLimit", "Уведомление",
                String.format("Начал действовать новый лимит расходов в размере %.2f...", newCurrentLimit.getAmount()),
                messageCaptor.getValue().getContent());
    }

    @Test
    void testLimitAutoRenewal() {
        LocalDate startDate = LocalDate.of(2024, 10, 27);
        System.setProperty("expense-limit.current-date", startDate.toString());

        printTestInfo("testLimitAutoRenewal", "Начальная дата", startDate, getCurrentDate());

        // Создаем лимит с автопродлением
        ExpenseLimit limit = createAndSaveLimit(new BigDecimal("1000.00"), ExpenseLimit.LimitPeriod.WEEKLY, startDate, true);

        printTestInfo("testLimitAutoRenewal", "Создан лимит с автопродлением",
                String.format("startDate: %s, endDate: %s", startDate, startDate.plusDays(6)),
                String.format("startDate: %s, endDate: %s", limit.getStartDate(), limit.getEndDate()));

        // Симулируем прохождение времени до дня окончания лимита
        LocalDate endDate = startDate.plusDays(7);
        System.setProperty("expense-limit.current-date", endDate.toString());

        printTestInfo("testLimitAutoRenewal", "Новая дата", endDate, getCurrentDate());

        expenseLimitService.checkAndResetLimitIfNeeded();

        ExpenseLimit renewedLimit = expenseLimitService.getCurrentLimit();

        assertNotNull(renewedLimit, "Обновленный лимит не должен быть null");
        assertEquals(new BigDecimal("1000.00"), renewedLimit.getAmount(), "Сумма продленного лимита должна остаться прежней");
        assertEquals(endDate, renewedLimit.getStartDate(), "Продленный лимит должен начинаться с даты окончания предыдущего");
        assertEquals(endDate.plusDays(6), renewedLimit.getEndDate(), "Продленный лимит должен заканчиваться через неделю после начала");

        printTestInfo("testLimitAutoRenewal", "Обновленный лимит",
                String.format("amount: 1000.00, startDate: %s, endDate: %s",
                        endDate, endDate.plusDays(6)),
                String.format("amount: %.2f, startDate: %s, endDate: %s",
                        renewedLimit.getAmount(), renewedLimit.getStartDate(), renewedLimit.getEndDate()));

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mailEventPublisherService).publishMailCreatedEvent(messageCaptor.capture());
        printTestInfo("testLimitAutoRenewal", "Уведомление",
                String.format("Ваш лимит расходов в размере %.2f был автоматически продлен...", renewedLimit.getAmount()),
                messageCaptor.getValue().getContent());

        // Проверяем отправку уведомления о продлении лимита
        verifyLimitNotification(renewedLimit, false);
    }

    @Test
    void testLimitExpiration() {
        LocalDate startDate = LocalDate.of(2024, 10, 27);
        System.setProperty("expense-limit.current-date", startDate.toString());
        printTestInfo("testLimitExpiration", "Начальная дата", startDate, getCurrentDate());

        // Создаем лимит без автопродления
        ExpenseLimit limit = createAndSaveLimit(new BigDecimal("1000.00"),
                ExpenseLimit.LimitPeriod.WEEKLY, startDate, false);
        printTestInfo("testLimitExpiration", "Создан лимит без автопродления",
                String.format("startDate: %s, endDate: %s, autoRenew: false",
                        startDate, startDate.plusDays(6)),
                String.format("startDate: %s, endDate: %s, autoRenew: %s",
                        limit.getStartDate(), limit.getEndDate(), limit.isAutoRenew()));

        // Симулируем прохождение времени до дня окончания лимита
        LocalDate endDate = startDate.plusDays(7);
        System.setProperty("expense-limit.current-date", endDate.toString());
        printTestInfo("testLimitExpiration", "Новая дата", endDate, getCurrentDate());

        expenseLimitService.checkAndResetLimitIfNeeded();

        // Проверяем, что лимит истек
        ExpenseLimit expiredLimit = expenseLimitService.getCurrentLimit();
        printTestInfo("testLimitExpiration", "Истекший лимит",
                "null (лимит должен быть истекшим)",
                expiredLimit);

        // Проверяем отправку уведомления об истечении лимита
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mailEventPublisherService).publishMailCreatedEvent(messageCaptor.capture());

        String expectedMessage = String.format(
                "Ваш лимит расходов в размере %.2f истек. " +
                        "Тип периода: '%s'. Период: %s - %s. " +
                        "Для установки нового лимита, пожалуйста, войдите в систему.",
                limit.getAmount(),
                limit.getPeriod().getTitle(),
                limit.getStartDate(),
                limit.getEndDate()
        );

        printTestInfo("testLimitExpiration", "Уведомление об истечении лимита",
                expectedMessage,
                messageCaptor.getValue().getContent());

        assertEquals(expectedMessage, messageCaptor.getValue().getContent(),
                "Сообщение должно точно соответствовать ожидаемому формату");
    }

    @Test
    void testCalculateEndDate() {
        printTestInfo("testCalculateEndDate", "Начало теста",
                "Проверка расчета даты окончания для разных периодов",
                "Создание тестового лимита");

        ExpenseLimit limit = new ExpenseLimit();
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        printTestInfo("testCalculateEndDate", "Начальная дата",
                startDate.toString(),
                "Будет использоваться для всех проверок");

        // Проверка недельного периода
        limit.setLimitPeriod(ExpenseLimit.LimitPeriod.WEEKLY, startDate);
        printTestInfo("testCalculateEndDate", "Недельный период",
                String.format("Ожидаемая дата окончания: %s", startDate.plusWeeks(1).minusDays(1)),
                String.format("Фактическая дата окончания: %s", limit.getEndDate()));
        assertEquals(startDate.plusWeeks(1).minusDays(1), limit.getEndDate(),
                "Неделя должна заканчиваться через 6 дней после начала");

        // Проверка месячного периода
        limit.setLimitPeriod(ExpenseLimit.LimitPeriod.MONTHLY, startDate);
        printTestInfo("testCalculateEndDate", "Месячный период",
                String.format("Ожидаемая дата окончания: %s", startDate.plusMonths(1).minusDays(1)),
                String.format("Фактическая дата окончания: %s", limit.getEndDate()));
        assertEquals(startDate.plusMonths(1).minusDays(1), limit.getEndDate(),
                "Месяц должен заканчиваться в последний день месяца");

        // Проверка бессрочного периода
        limit.setLimitPeriod(ExpenseLimit.LimitPeriod.INDEFINITE, startDate);
        printTestInfo("testCalculateEndDate", "Бессрочный период",
                "Ожидаемая дата окончания: null",
                String.format("Фактическая дата окончания: %s", limit.getEndDate()));
        assertNull(limit.getEndDate(),
                "Бессрочный период не должен иметь даты окончания");

        // Проверка граничных случаев
        printTestInfo("testCalculateEndDate", "Проверка граничных случаев",
                "Установка null значений",
                "Проверка обработки null");

        limit.setLimitPeriod(null, startDate);
        printTestInfo("testCalculateEndDate", "Null период",
                "Ожидаемая дата окончания: null",
                String.format("Фактическая дата окончания: %s", limit.getEndDate()));
        assertNull(limit.getEndDate(),
                "При null периоде дата окончания должна быть null");

        limit.setLimitPeriod(ExpenseLimit.LimitPeriod.WEEKLY, null);
        printTestInfo("testCalculateEndDate", "Null дата начала",
                "Ожидаемая дата окончания: null",
                String.format("Фактическая дата окончания: %s", limit.getEndDate()));
        assertNull(limit.getEndDate(),
                "При null дате начала дата окончания должна быть null");

        // Итоговый результат
        printTestInfo("testCalculateEndDate", "Результат теста",
                "Все проверки выполнены успешно",
                "Расчет дат окончания работает корректно для всех периодов");
    }

    private ExpenseLimit createAndSaveLimit(BigDecimal amount, ExpenseLimit.LimitPeriod period, LocalDate startDate, boolean autoRenew) {
        ExpenseLimit limit = new ExpenseLimit();
        limit.setAmount(amount);
        limit.setLimitPeriod(period, startDate);
        limit.setAutoRenew(autoRenew);
        log.info("Создание лимита: amount={}, period={}, startDate={}, autoRenew={}", amount, period, startDate, autoRenew);
        ExpenseLimit savedLimit = expenseLimitRepository.save(limit);
        log.info("Лимит сохранен: {}", savedLimit);
        return savedLimit;
    }

    private void verifyRenewalNotification(ExpenseLimit limit, LocalDate oldStartDate, LocalDate oldEndDate, LocalDate newStartDate) {
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mailEventPublisherService).publishMailCreatedEvent(messageCaptor.capture());
        SendMessage capturedMessage = messageCaptor.getValue();

        String expectedMessage = String.format(
                "Ваш лимит расходов в размере %.2f был автоматически продлен. " +
                        "Тип периода: '%s'. Старый период: %s - %s. Новый период начался с %s. Автопродление: %s.",
                limit.getAmount(),
                limit.getPeriod().getTitle(),
                oldStartDate, oldEndDate, newStartDate,
                limit.isAutoRenew() ? "включено" : "выключено"
        );

        assertEquals(expectedMessage, capturedMessage.getContent(),
                "Сообщение должно точно соответствовать ожидаемому формату");
    }

    private void verifyLimitNotification(ExpenseLimit limit, boolean isNewLimit) {
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(mailEventPublisherService).publishMailCreatedEvent(messageCaptor.capture());
        SendMessage capturedMessage = messageCaptor.getValue();

        String expectedMessage;
        if (isNewLimit) {
            expectedMessage = String.format(
                    "Начал действовать новый лимит расходов в размере %.2f. " +
                            "Тип периода: '%s'. Период: %s - %s.",
                    limit.getAmount(),
                    limit.getPeriod().getTitle(),
                    limit.getStartDate(), limit.getEndDate()
            );
        } else {
            expectedMessage = String.format(
                    "Ваш лимит расходов в размере %.2f был автоматически продлен. " +
                            "Тип периода: '%s'. Старый период: %s - %s. " +
                            "Новый период начался с %s. Автопродление: включено.",
                    limit.getAmount(),
                    limit.getPeriod().getTitle(),
                    limit.getStartDate().minusDays(7), limit.getStartDate().minusDays(1),
                    limit.getStartDate()
            );
        }

        assertEquals(expectedMessage, capturedMessage.getContent(),
                "Сообщение должно точно соответствовать ожидаемому формату");
    }

    private void printTestInfo(String testName, String checkType, Object expected, Object actual) {
        System.out.println("\n--- " + testName + " ---");
        System.out.println(checkType + ":");
        System.out.println("  Ожидаемое значение: " + expected);
        System.out.println("  Фактическое значение: " + actual);
    }

    private LocalDate getCurrentDate() {
        String overrideDate = System.getProperty("expense-limit.current-date");
        return (overrideDate != null && !overrideDate.isEmpty())
                ? LocalDate.parse(overrideDate)
                : LocalDate.now();
    }
}
