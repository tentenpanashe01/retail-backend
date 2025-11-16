package com.company.retail.purchaseorder;

import com.company.retail.expense.ExpenseModel;
import com.company.retail.expense.ExpenseRepository;
import com.company.retail.product.ProductModel;
import com.company.retail.purchaseOrderItem.PurchaseOrderItemModel;
import com.company.retail.purchaseOrderItem.PurchaseOrderItemRepository;
import com.company.retail.ShopStock.ShopStockModel;
import com.company.retail.ShopStock.ShopStockRepository;
import com.company.retail.shop.ShopModel;
import com.company.retail.shop.ShopRepository;
import com.company.retail.user.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final ExpenseRepository expenseRepository;
    private final ShopStockRepository shopStockRepository;
    private final ShopRepository shopRepository;

    // ✅ Get all orders
    public List<PurchaseOrderModel> getAllOrders() {
        return purchaseOrderRepository.findAll();
    }

    // ✅ Get orders by shop
    public List<PurchaseOrderModel> getOrdersByShop(Long shopId) {
        ShopModel shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with ID: " + shopId));
        return purchaseOrderRepository.findByShop(shop);
    }

    // ✅ Get one order
    public PurchaseOrderModel getOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found with ID " + id));
    }

    // ✅ Create new order
    public PurchaseOrderModel createOrder(PurchaseOrderModel order) {
        if (order.getShop() == null) {
            throw new RuntimeException("Shop must be specified when creating a purchase order.");
        }

        //Capture logged-in user
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserModel) {
            order.setCreatedBy((UserModel) principal);
        }
        order.setStatus(PurchaseOrderModel.Status.PENDING);
        order.setOrderDate(java.time.LocalDateTime.now());
        return purchaseOrderRepository.save(order);
    }

    // ✅ Update order details (supplier, totals, etc.)
    public PurchaseOrderModel updateOrder(Long id, PurchaseOrderModel updatedOrder) {
        PurchaseOrderModel existing = getOrderById(id);

        if (updatedOrder.getSupplierName() != null)
            existing.setSupplierName(updatedOrder.getSupplierName());
        if (updatedOrder.getTotalCostUSD() != null)
            existing.setTotalCostUSD(updatedOrder.getTotalCostUSD());
        if (updatedOrder.getTotalCostZWL() != null)
            existing.setTotalCostZWL(updatedOrder.getTotalCostZWL());

        return purchaseOrderRepository.save(existing);
    }

    // ✅ Delete order
    public void deleteOrder(Long id) {
        if (!purchaseOrderRepository.existsById(id)) {
            throw new RuntimeException("Purchase Order not found");
        }
        purchaseOrderRepository.deleteById(id);
    }

    // ✅ Get all expenses linked to this order
    public List<ExpenseModel> getOrderExpenses(Long orderId) {
        return expenseRepository.findByPurchaseOrder_PurchaseOrderId(orderId);
    }

    // ✅ Get total expenses for this order
    public double getTotalExpenses(Long orderId) {
        return expenseRepository.findByPurchaseOrder_PurchaseOrderId(orderId)
                .stream()
                .mapToDouble(e -> e.getAmountUSD() != null ? e.getAmountUSD() : 0.0)
                .sum();
    }

    // ✅ Compute landing cost and update shop stock
    @Transactional
    public PurchaseOrderModel calculateLandingCost(Long orderId) {
        PurchaseOrderModel order = getOrderById(orderId);
        List<PurchaseOrderItemModel> items = purchaseOrderItemRepository.findByPurchaseOrder_PurchaseOrderId(orderId);
        List<ExpenseModel> expenses = expenseRepository.findByPurchaseOrder_PurchaseOrderId(orderId);

        if (items.isEmpty()) {
            throw new RuntimeException("Cannot calculate landing cost — no items in order.");
        }

        double totalExpenseUSD = expenses.stream()
                .mapToDouble(e -> e.getAmountUSD() == null ? 0.0 : e.getAmountUSD())
                .sum();

        double totalExpenseZWL = expenses.stream()
                .mapToDouble(e -> e.getAmountZWL() == null ? 0.0 : e.getAmountZWL())
                .sum();

        double totalItemCostUSD = items.stream().mapToDouble(PurchaseOrderItemModel::getTotalCostUSD).sum();
        double totalItemCostZWL = items.stream().mapToDouble(PurchaseOrderItemModel::getTotalCostZWL).sum();

        // ✅ Store totals in PurchaseOrder
        order.setExpensesUSD(totalExpenseUSD);
        order.setExpensesZWL(totalExpenseZWL);
        order.setTotalCostUSD(totalItemCostUSD);
        order.setTotalCostZWL(totalItemCostZWL);

        // ✅ Compute landing costs for each item
        for (PurchaseOrderItemModel item : items) {
            ProductModel product = item.getProduct();

            double itemShareUSD = totalItemCostUSD == 0 ? 0 : item.getTotalCostUSD() / totalItemCostUSD;
            double itemShareZWL = totalItemCostZWL == 0 ? 0 : item.getTotalCostZWL() / totalItemCostZWL;

            double allocatedExpenseUSD = totalExpenseUSD * itemShareUSD;
            double allocatedExpenseZWL = totalExpenseZWL * itemShareZWL;

            double landingUSD = (item.getTotalCostUSD() + allocatedExpenseUSD) / item.getQuantity();
            double landingZWL = (item.getTotalCostZWL() + allocatedExpenseZWL) / item.getQuantity();

            ShopStockModel shopStock = shopStockRepository
                    .findByShopAndProduct(order.getShop(), product)
                    .orElseGet(() -> new ShopStockModel(order.getShop(), product));

            double existingQty = shopStock.getQuantityInStock() == null ? 0.0 : shopStock.getQuantityInStock();
            double existingCostUSD = shopStock.getAvgLandingCostUSD() == null ? 0.0 : shopStock.getAvgLandingCostUSD();
            double existingCostZWL = shopStock.getAvgLandingCostZWL() == null ? 0.0 : shopStock.getAvgLandingCostZWL();

            double totalQty = existingQty + item.getQuantity();

            double newAvgCostUSD = totalQty == 0 ? landingUSD :
                    ((existingCostUSD * existingQty) + (landingUSD * item.getQuantity())) / totalQty;
            double newAvgCostZWL = totalQty == 0 ? landingZWL :
                    ((existingCostZWL * existingQty) + (landingZWL * item.getQuantity())) / totalQty;

            shopStock.setAvgLandingCostUSD(newAvgCostUSD);
            shopStock.setAvgLandingCostZWL(newAvgCostZWL);
            shopStock.setQuantityInStock((int) totalQty);

            shopStockRepository.save(shopStock);
        }

        // ✅ Save and return updated order
        return purchaseOrderRepository.save(order);
    }

    // ✅ Mark order as completed — actually increment stock
    @Transactional
    public PurchaseOrderModel markOrderAsCompleted(Long orderId) {
        PurchaseOrderModel order = getOrderById(orderId);
        List<PurchaseOrderItemModel> items = purchaseOrderItemRepository.findByPurchaseOrder_PurchaseOrderId(orderId);
        List<ExpenseModel> expenses = expenseRepository.findByPurchaseOrder_PurchaseOrderId(orderId);

        if (items.isEmpty()) {
            throw new RuntimeException("Cannot complete order — no products found.");
        }
        if (expenses.isEmpty()) {
            throw new RuntimeException("Cannot complete order — no expenses recorded.");
        }

        // ✅ 1. Calculate landing cost and update stocks (updates average cost also).
        calculateLandingCost(orderId);


        // ✅ 2. Mark order as complete and set received date
        order.setStatus(PurchaseOrderModel.Status.COMPLETED);
        order.setReceivedDate(java.time.LocalDateTime.now());
        return purchaseOrderRepository.save(order);
    }

    // ✅ Auto-generate order (placeholder)
    public PurchaseOrderModel autoGenerateOrder() {
        PurchaseOrderModel order = new PurchaseOrderModel();
        order.setStatus(PurchaseOrderModel.Status.PENDING);
        order.setOrderDate(java.time.LocalDateTime.now());
        return purchaseOrderRepository.save(order);
    }
}