package com.company.retail.expensecategory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategoryModel, Long> {
    boolean existsByName(String name);
}
