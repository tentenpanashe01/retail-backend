package com.company.retail.purchaseOrderItem;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-order-items")
@RequiredArgsConstructor
public class PurchaseOrderItemController {

    private final PurchaseOrderItemService purchaseOrderItemService;

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @GetMapping
    public ResponseEntity<List<PurchaseOrderItemModel>> getAllItems() {
        return ResponseEntity.ok(purchaseOrderItemService.getAllItems());
    }

    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN', 'SUPERADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(purchaseOrderItemService.getItemById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("❌ " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN', 'SUPERADMIN')")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getItemsByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(purchaseOrderItemService.getItemsByOrder(orderId));
    }

    /** ✅ SUPERVISOR, ADMIN, or SUPERADMIN can add items to orders */
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN', 'SUPERADMIN')")
    @PostMapping
    public ResponseEntity<?> createPurchaseOrderItem(@RequestBody PurchaseOrderItemModel request) {
        return ResponseEntity.ok(purchaseOrderItemService.createPurchaseOrderItem(request));
    }

    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN', 'SUPERADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePurchaseOrderItem(@PathVariable Long id,
                                                     @RequestBody PurchaseOrderItemModel updatedItem) {
        return ResponseEntity.ok(purchaseOrderItemService.updatePurchaseOrderItem(id, updatedItem));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePurchaseOrderItem(@PathVariable Long id) {
        purchaseOrderItemService.deletePurchaseOrderItem(id);
        return ResponseEntity.ok("✅ Deleted successfully");
    }
}