package com.company.retail.expense;

import com.company.retail.purchaseorder.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    // ✅ Record expense
    public ExpenseModel addExpense(ExpenseModel expense) {
        return expenseRepository.save(expense);
    }

    // ✅ Get all expenses for a given purchase order
    public List<ExpenseModel> getExpensesByPurchaseOrder(Long purchaseOrderId) {
        return expenseRepository.findByPurchaseOrder_PurchaseOrderId(purchaseOrderId);
    }

    // ✅ Calculate landing cost (total of all expenses in a purchase order)
    public BigDecimal calculateLandingCost(Long purchaseOrderId) {
        List<ExpenseModel> expenses = expenseRepository.findByPurchaseOrder_PurchaseOrderId(purchaseOrderId);
        return expenses.stream()
                .map(expense -> BigDecimal.valueOf(expense.getAmountUSD()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ✅ Get all operational expenses for a shop
    public List<ExpenseModel> getShopOperationalExpenses(Long shopId) {
        return expenseRepository.findByShop_Id(shopId)
                .stream()
                .filter(e -> e.getExpenseType() == ExpenseModel.ExpenseType.OPERATIONAL)
                .toList();
    }

}
