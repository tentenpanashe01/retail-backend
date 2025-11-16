package com.company.retail.purchaseOrderItem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseOrderItemRequest {
    private Long purchaseOrderId;
    private Long productId;
    private Integer quantity;
    private Double unitPurchasePriceUSD;
    private Double unitPurchasePriceZWL;
}