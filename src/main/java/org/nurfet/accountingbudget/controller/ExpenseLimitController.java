package org.nurfet.accountingbudget.controller;

import lombok.RequiredArgsConstructor;
import org.nurfet.accountingbudget.model.ExpenseLimit;
import org.nurfet.accountingbudget.service.ExpenseLimitService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/expense-limit")
public class ExpenseLimitController {

    private final ExpenseLimitService expenseLimitService;

    @GetMapping
    public String showLimitForm(Model model) {
        ExpenseLimit currentLimit = expenseLimitService.getCurrentLimit();
        model.addAttribute("currentLimit", currentLimit);
        model.addAttribute("newLimit", new ExpenseLimit());
        return "expense-limit";
    }

    @PostMapping("/set")
    public String setLimit(@ModelAttribute ExpenseLimit newLimit) {
        expenseLimitService.setLimit(newLimit.getAmount(), newLimit.getPeriod());
        return "redirect:/expense-limit";
    }

    @PostMapping("/remove")
    public String removeLimit() {
        expenseLimitService.removeLimit();
        return "redirect:/transactions";
    }
}
