package com.company.retail.purchaseOrderItem;

import com.company.retail.product.ProductModel;
import com.company.retail.purchaseorder.PurchaseOrderModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "purchase_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItemModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseOrderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PurchaseOrderModel purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductModel product;

    private Integer quantity;
    private Double unitPurchasePriceUSD;
    private Double unitPurchasePriceZWL;
    private Double totalCostUSD;
    private Double totalCostZWL;
}

