package com.company.retail.purchaseorder;

import com.company.retail.expense.ExpenseModel;
import com.company.retail.purchaseOrderItem.PurchaseOrderItemModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    /** ✅ ADMIN & SUPERADMIN can view all orders */
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @GetMapping
    public ResponseEntity<List<PurchaseOrderModel>> getAllOrders() {
        return ResponseEntity.ok(purchaseOrderService.getAllOrders());
    }

    /** ✅ SUPERVISOR, ADMIN & SUPERADMIN - Get all orders for a specific shop */
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN','SUPERADMIN')")
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<PurchaseOrderModel>> getOrdersByShop(@PathVariable Long shopId) {
        try {
            List<PurchaseOrderModel> orders = purchaseOrderService.getOrdersByShop(shopId);
            return ResponseEntity.ok(orders);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    /** ✅ SUPERVISOR, ADMIN & SUPERADMIN - Get single order by ID */
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN','SUPERADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(purchaseOrderService.getOrderById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("❌ " + e.getMessage());
        }
    }

    /** ✅ SUPERVISOR, ADMIN, SUPERADMIN can create orders */
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN','SUPERADMIN')")
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody PurchaseOrderModel order) {
        try {
            return ResponseEntity.ok(purchaseOrderService.createOrder(order));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }

    /** ✅ SUPERVISOR, ADMIN, SUPERADMIN can update orders */
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN','SUPERADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody PurchaseOrderModel order) {
        try {
            return ResponseEntity.ok(purchaseOrderService.updateOrder(id, order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }

    /** ✅ ADMIN & SUPERADMIN can delete orders */
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            purchaseOrderService.deleteOrder(id);
            return ResponseEntity.ok("✅ Deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("❌ " + e.getMessage());
        }
    }

    /** ✅ SUPERVISOR, ADMIN, SUPERADMIN - Get order items */
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN','SUPERADMIN')")
    @GetMapping("/{orderId}/items")
    public ResponseEntity<?> getOrderItems(@PathVariable Long orderId) {
        try {
            List<PurchaseOrderItemModel> items = purchaseOrderService.getOrderById(orderId).getItems();
            return ResponseEntity.ok(items);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("❌ " + e.getMessage());
        }
    }

    /** ✅ SUPERVISOR, ADMIN, SUPERADMIN - Get order expenses */
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN','SUPERADMIN')")
    @GetMapping("/{orderId}/expenses")
    public ResponseEntity<?> getOrderExpenses(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(purchaseOrderService.getOrderExpenses(orderId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("❌ " + e.getMessage());
        }
    }

    /** ✅ SUPERVISOR, ADMIN, SUPERADMIN - Get total expenses for an order */
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN','SUPERADMIN')")
    @GetMapping("/{orderId}/expenses/total")
    public ResponseEntity<?> getOrderTotalExpenses(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(purchaseOrderService.getTotalExpenses(orderId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }

    /** ✅ ADMIN, SUPERADMIN - Recalculate landing cost */
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @GetMapping("/{orderId}/landing-cost")
    public ResponseEntity<?> calculateLandingCost(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(purchaseOrderService.calculateLandingCost(orderId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }

    /** ✅ SUPERVISOR, ADMIN, SUPERADMIN - Mark order complete */
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN','SUPERADMIN')")
    @PatchMapping("/{orderId}/complete")
    public ResponseEntity<?> markOrderAsCompleted(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(purchaseOrderService.markOrderAsCompleted(orderId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }

    /** ✅ ADMIN, SUPERADMIN - Auto-generate purchase order */
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @PostMapping("/auto-generate")
    public ResponseEntity<?> autoGenerateOrder() {
        try {
            return ResponseEntity.ok(purchaseOrderService.autoGenerateOrder());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }
}