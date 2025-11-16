package com.company.retail.purchaseorder;

import com.company.retail.purchaseOrderItem.PurchaseOrderItemModel;
import com.company.retail.shop.ShopModel;
import com.company.retail.user.UserModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseOrderId;

    private String supplierName;
    private LocalDateTime orderDate;
    private LocalDateTime receivedDate;

    @Enumerated(EnumType.STRING)
    private Status status; // Pending, Completed, Cancelled

    private Double totalCostUSD;
    private Double totalCostZWL;
    private Double expensesUSD;
    private Double expensesZWL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UserModel createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ShopModel shop;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL)
    private List<PurchaseOrderItemModel> items;

    public enum Status {
        PENDING, COMPLETED, CANCELLED
    }

    // ✅ Lightweight constructor for ID-only references
    public PurchaseOrderModel(Long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }




}
