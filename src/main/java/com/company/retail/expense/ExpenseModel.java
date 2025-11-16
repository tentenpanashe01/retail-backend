package com.company.retail.expense;

import com.company.retail.expensecategory.ExpenseCategoryModel;
import com.company.retail.purchaseorder.PurchaseOrderModel;
import com.company.retail.shop.ShopModel;
import com.company.retail.user.UserModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long expenseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id") // nullable(for operational expenses)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ShopModel shop;

    @Enumerated(EnumType.STRING)
    private ExpenseType expenseType; // PURCHASE or OPERATIONAL


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PurchaseOrderModel purchaseOrder; // nullable → allows non-PO expenses

    private Double amountUSD;
    private Double amountZWL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
   private ExpenseCategoryModel category;

    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UserModel recordedBy;

    private String description;

    public enum ExpenseType {
        OPERATIONAL,
        PURCHASE,
    }
}
