package com.company.retail.purchaseorder;

import com.company.retail.shop.ShopModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderModel, Long> {
    List<PurchaseOrderModel> findByShop(ShopModel shop);

    // ✅ Fetch all orders by their status
    List<PurchaseOrderModel> findByStatus(PurchaseOrderModel.Status status);
}
