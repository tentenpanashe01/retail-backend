package com.company.retail.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<ExpenseModel, Long> {
    List<ExpenseModel> findByPurchaseOrder_PurchaseOrderId(Long purchaseOrderId);
    List<ExpenseModel> findByShop_Id(Long shopId);


}
