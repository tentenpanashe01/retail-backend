package com.company.retail.expensecategory;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expense-categories")
@RequiredArgsConstructor
public class ExpenseCategoryController {

    private final ExpenseCategoryService categoryService;

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'SUPERADMIN')")
    @PostMapping
    public ResponseEntity<ExpenseCategoryModel> createCategory(@RequestBody ExpenseCategoryModel category) {
        return ResponseEntity.ok(categoryService.createCategory(category));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<ExpenseCategoryModel>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'SUPERADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseCategoryModel> updateCategory(@PathVariable Long id, @RequestBody ExpenseCategoryModel category) {
        return ResponseEntity.ok(categoryService.updateCategory(id, category));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}