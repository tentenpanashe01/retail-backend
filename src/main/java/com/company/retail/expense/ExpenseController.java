package com.company.retail.expense;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * ✅ SUPERVISOR or ADMIN can record new expenses for their shop
     */
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN', 'SUPERADMIN')")
    @PostMapping
    public ResponseEntity<ExpenseModel> addExpense(@RequestBody ExpenseModel expense) {
        return ResponseEntity.ok(expenseService.addExpense(expense));
    }

    /**
     * ✅ SUPERVISOR, ADMIN, or SUPERADMIN can view purchase order expenses
     */
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN', 'SUPERADMIN')")
    @GetMapping("/purchase-order/{id}")
    public ResponseEntity<List<ExpenseModel>> getExpensesByPurchaseOrder(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpensesByPurchaseOrder(id));
    }

    /**
     * ✅ SUPERVISOR, ADMIN, or SUPERADMIN can calculate landing cost
     */
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN', 'SUPERADMIN')")
    @GetMapping("/purchase-order/{id}/landing-cost")
    public ResponseEntity<BigDecimal> getLandingCost(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.calculateLandingCost(id));
    }

    /**
     * ✅ SUPERVISOR can view operational expenses for their shop
     * ✅ ADMIN & SUPERADMIN can view any shop
     */
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN', 'SUPERADMIN')")
    @GetMapping("/shop/{shopId}/operational")
    public ResponseEntity<List<ExpenseModel>> getShopOperationalExpenses(@PathVariable Long shopId) {
        return ResponseEntity.ok(expenseService.getShopOperationalExpenses(shopId));
    }
}