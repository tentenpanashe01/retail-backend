package com.company.retail.expensecategory;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository categoryRepository;

    public ExpenseCategoryModel createCategory(ExpenseCategoryModel category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category already exists");
        }
        return categoryRepository.save(category);
    }

    public List<ExpenseCategoryModel> getAllCategories() {
        return categoryRepository.findAll();
    }

    public ExpenseCategoryModel updateCategory(Long id, ExpenseCategoryModel updated) {
        ExpenseCategoryModel existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        return categoryRepository.save(existing);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
