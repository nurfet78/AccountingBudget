package org.nurfet.accountingbudget.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nurfet.accountingbudget.model.Category;
import org.nurfet.accountingbudget.model.TransactionType;
import org.nurfet.accountingbudget.service.CategoryService;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "categories";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("transactionTypes", TransactionType.values());
        return "category-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.getCategoryById(id));
        model.addAttribute("transactionTypes", TransactionType.values());
        return "category-form";
    }

    @PostMapping("/save")
    public String saveCategory(@Valid @ModelAttribute Category category, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("transactionTypes", TransactionType.values());
            return "category-form";
        }

        if (categoryService.existsCategoryByName(category.getName())) {
            model.addAttribute("error", "Категория уже существует");
            model.addAttribute("transactionTypes", TransactionType.values());
            return "category-form";
        }

        if (category.getId() == null) {
            categoryService.createCategory(category);
        } else {
            categoryService.updateCategory(category.getId(), category);
        }
        return "redirect:/categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/categories";
    }
}
