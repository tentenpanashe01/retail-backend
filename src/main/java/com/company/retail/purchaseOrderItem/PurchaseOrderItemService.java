package com.company.retail.purchaseOrderItem;

import com.company.retail.product.ProductModel;
import com.company.retail.product.ProductRepository;
import com.company.retail.purchaseorder.PurchaseOrderModel;
import com.company.retail.purchaseorder.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderItemService {

    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;

    /**
     * ✅ Get all purchase order items
     */
    public List<PurchaseOrderItemModel> getAllItems() {
        return purchaseOrderItemRepository.findAll();
    }

    /**
     * ✅ Get item by ID
     */
    public PurchaseOrderItemModel getItemById(Long id) {
        return purchaseOrderItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order Item not found"));
    }

    /**
     * ✅ Get all items in a specific purchase order
     */
    public List<PurchaseOrderItemModel> getItemsByOrder(Long purchaseOrderId) {
        return purchaseOrderItemRepository.findByPurchaseOrder_PurchaseOrderId(purchaseOrderId);
    }

    /**
     * ✅ Create or update a purchase order item
     * If the same product already exists in the same order → increase quantity instead of duplicate entry.
     */
    @Transactional
    public PurchaseOrderItemModel createPurchaseOrderItem(PurchaseOrderItemModel item) {

        // Validate purchase order
        PurchaseOrderModel order = purchaseOrderRepository.findById(item.getPurchaseOrder().getPurchaseOrderId())
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));

        // Validate product
        ProductModel product = productRepository.findById(item.getProduct().getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if item already exists for this order & product
        PurchaseOrderItemModel existingItem = purchaseOrderItemRepository
                .findByPurchaseOrderAndProduct(order, product)
                .orElse(null);

        if (existingItem != null) {
            // 🔁 Update existing record (add quantities)
            int newQty = existingItem.getQuantity() + item.getQuantity();
            existingItem.setQuantity(newQty);
            existingItem.setUnitPurchasePriceUSD(item.getUnitPurchasePriceUSD());
            existingItem.setUnitPurchasePriceZWL(item.getUnitPurchasePriceZWL());
            existingItem.setTotalCostUSD(item.getUnitPurchasePriceUSD() * newQty);
            existingItem.setTotalCostZWL(item.getUnitPurchasePriceZWL() * newQty);

            return purchaseOrderItemRepository.save(existingItem);
        }

        // 💰 Compute totals for a new item
        item.setTotalCostUSD(item.getUnitPurchasePriceUSD() * item.getQuantity());
        item.setTotalCostZWL(item.getUnitPurchasePriceZWL() * item.getQuantity());

        // 🏗️ Save the new item
        PurchaseOrderItemModel savedItem = purchaseOrderItemRepository.save(item);

        // 📦 Update stock if the order is already completed
        if (order.getStatus() == PurchaseOrderModel.Status.COMPLETED) {
            // We’ll rely on stock module to handle per-shop updates later
            // (Purchase order completion triggers landing cost + stock updates)
        }

        return savedItem;
    }

    /**
     * ✅ Update an existing purchase order item
     */
    @Transactional
    public PurchaseOrderItemModel updatePurchaseOrderItem(Long id, PurchaseOrderItemModel updatedItem) {
        PurchaseOrderItemModel existing = purchaseOrderItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order item not found"));

        existing.setQuantity(updatedItem.getQuantity());
        existing.setUnitPurchasePriceUSD(updatedItem.getUnitPurchasePriceUSD());
        existing.setUnitPurchasePriceZWL(updatedItem.getUnitPurchasePriceZWL());
        existing.setTotalCostUSD(updatedItem.getQuantity() * updatedItem.getUnitPurchasePriceUSD());
        existing.setTotalCostZWL(updatedItem.getQuantity() * updatedItem.getUnitPurchasePriceZWL());

        return purchaseOrderItemRepository.save(existing);
    }

    /**
     * ✅ Delete a purchase order item
     */
    @Transactional
    public void deletePurchaseOrderItem(Long id) {
        purchaseOrderItemRepository.deleteById(id);
    }
}
