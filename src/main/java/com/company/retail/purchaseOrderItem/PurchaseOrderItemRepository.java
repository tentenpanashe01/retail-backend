package com.company.retail.purchaseOrderItem;

import com.company.retail.product.ProductModel;
import com.company.retail.purchaseorder.PurchaseOrderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItemModel, Long> {

    // ✅ Get all items for a given order
    List<PurchaseOrderItemModel> findByPurchaseOrder_PurchaseOrderId(Long purchaseOrderId);

    // ✅ Find a specific product already linked to a given order
    Optional<PurchaseOrderItemModel> findByPurchaseOrderAndProduct(
            PurchaseOrderModel purchaseOrder,
            ProductModel product
    );
}
