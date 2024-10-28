package org.nurfet.accountingbudget.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.accountingbudget.model.ExpenseLimit;
import org.nurfet.accountingbudget.service.ExpenseLimitService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;


@Controller
@RequiredArgsConstructor
@RequestMapping("/expense-limit")
@Slf4j
public class ExpenseLimitController {

    private final ExpenseLimitService expenseLimitService;

    @GetMapping
    public String showLimitForm(Model model) {
        ExpenseLimit currentLimit = expenseLimitService.getCurrentLimit();
        model.addAttribute("currentLimit", currentLimit);

        ExpenseLimit futureLimit = expenseLimitService.getFutureLimit();
        model.addAttribute("futureLimit", futureLimit);

        model.addAttribute("newLimit", new ExpenseLimit());
        return "expense-limit";
    }

    @PostMapping("/set")
    public String setLimit(@Valid @ModelAttribute("newLimit") ExpenseLimit newLimit, BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "expense-limit";
        }

        ExpenseLimit currentLimit = expenseLimitService.getCurrentLimit();
        ExpenseLimit futureLimit = expenseLimitService.getFutureLimit();

        if (currentLimit != null && futureLimit != null) {
            // Если есть и текущий, и будущий лимит, предлагаем заменить будущий
            redirectAttributes.addFlashAttribute("newLimit", newLimit);
            return "redirect:/expense-limit/replace-future";
        } else if (currentLimit != null && !LocalDate.now().isAfter(currentLimit.getEndDate())) {
            // Если есть только текущий лимит, перенаправляем на страницу подтверждения
            redirectAttributes.addFlashAttribute("newLimit", newLimit);
            return "redirect:/expense-limit/confirm";
        } else {
            // Если нет активного лимита, устанавливаем новый
            expenseLimitService.setOrUpdateLimit(newLimit.getAmount(), newLimit.getPeriod(), newLimit.isAutoRenew());
            redirectAttributes.addFlashAttribute("message", "Новый лимит успешно установлен");
            return "redirect:/expense-limit";
        }
    }

    @GetMapping("/replace-future")
    public String showReplaceFuturePage(@ModelAttribute("newLimit") ExpenseLimit newLimit, Model model) {
        ExpenseLimit currentLimit = expenseLimitService.getCurrentLimit();
        ExpenseLimit futureLimit = expenseLimitService.getFutureLimit();
        model.addAttribute("currentLimit", currentLimit);
        model.addAttribute("futureLimit", futureLimit);
        model.addAttribute("newLimit", newLimit);
        return "replace-future-limit";
    }

    @PostMapping("/replace-future")
    public String replaceFutureLimit(@ModelAttribute("newLimit") ExpenseLimit newLimit,
                                     RedirectAttributes redirectAttributes) {
        expenseLimitService.replaceFutureLimit(newLimit.getAmount(), newLimit.getPeriod(), newLimit.isAutoRenew());
        redirectAttributes.addFlashAttribute("message", "Будущий лимит успешно заменен");
        return "redirect:/expense-limit";
    }

    @PostMapping("/remove")
    public String removeLimit() {
        expenseLimitService.removeLimit();
        return "redirect:/transactions";
    }

    @GetMapping("/confirm")
    public String showConfirmPage(@ModelAttribute("newLimit") ExpenseLimit newLimit, Model model) {
        ExpenseLimit currentLimit = expenseLimitService.getCurrentLimit();
        model.addAttribute("currentLimit", currentLimit);
        model.addAttribute("newLimit", newLimit);
        return "confirm-limit-change";
    }

    @PostMapping("/confirm")
    public String confirmLimitChange(@ModelAttribute("newLimit") ExpenseLimit newLimit,
                                     RedirectAttributes redirectAttributes) {
        ExpenseLimit currentLimit = expenseLimitService.getCurrentLimit();

        if (currentLimit != null && LocalDate.now().isBefore(currentLimit.getEndDate())) {
            // Устанавливаем будущий лимит
            expenseLimitService.setFutureLimitAmount(newLimit.getAmount(), newLimit.getPeriod(), newLimit.isAutoRenew());
            redirectAttributes.addFlashAttribute("message", "Новый лимит будет применен после окончания текущего периода");
        } else {
            // Устанавливаем новый лимит немедленно
            expenseLimitService.setOrUpdateLimit(newLimit.getAmount(), newLimit.getPeriod(), newLimit.isAutoRenew());
            redirectAttributes.addFlashAttribute("message", "Новый лимит успешно установлен");
        }

        return "redirect:/expense-limit";
    }
}
