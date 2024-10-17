package org.nurfet.accountingbudget.service;

import org.nurfet.accountingbudget.model.Category;
import org.nurfet.accountingbudget.model.Transaction;

import java.util.List;

public interface CategoryService {

    List<Category> getAllCategories();

    Category createCategory(Category category);

    Category updateCategory(Long id, Category category);

    void deleteCategory(Long id);

    Category getCategoryById(Long id);
}
