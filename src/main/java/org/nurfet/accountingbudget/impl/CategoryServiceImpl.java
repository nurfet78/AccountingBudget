package org.nurfet.accountingbudget.impl;

import lombok.RequiredArgsConstructor;
import org.nurfet.accountingbudget.exception.NotFoundException;
import org.nurfet.accountingbudget.model.Category;
import org.nurfet.accountingbudget.repository.CategoryRepository;
import org.nurfet.accountingbudget.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;


    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, Category category) {
        Category existingCategory = getCategoryById(id);
        existingCategory.setName(category.getName());
        existingCategory.setDefaultType(category.getDefaultType());

        return categoryRepository.save(existingCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Category.class, id));
    }

    @Override
    public boolean existsCategoryByName(String categoryName) {
        return categoryRepository.existsByName(categoryName);
    }
}
