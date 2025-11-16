package com.company.retail.saleItem;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/sale-items")
@RequiredArgsConstructor
public class SaleItemController {

    private final SaleItemService saleItemService;

    /**
     * ✅ Get all sale items
     */
    @GetMapping
    public ResponseEntity<List<SaleItemModel>> getAllSaleItems() {
        return ResponseEntity.ok(saleItemService.getAllSaleItems());
    }

    /**
     * ✅ Get single sale item by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSaleItemById(@PathVariable Long id) {
        return saleItemService.getSaleItemById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body("❌ Sale item not found"));
    }

    /**
     * ✅ Get all items for a specific sale
     */
    @GetMapping("/sale/{saleId}")
    public ResponseEntity<List<SaleItemModel>> getItemsBySale(@PathVariable Long saleId) {
        return ResponseEntity.ok(saleItemService.getItemsBySale(saleId));
    }

    /**
     * ✅ Create a sale item
     */
    @PostMapping
    public ResponseEntity<?> createSaleItem(@RequestBody SaleItemModel item) {
        try {
            SaleItemModel created = saleItemService.createSaleItem(item);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }

    /**
     * ✅ Update sale item
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSaleItem(@PathVariable Long id, @RequestBody SaleItemModel item) {
        try {
            SaleItemModel updated = saleItemService.updateSaleItem(id, item);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }

    /**
     * ✅ Delete sale item
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSaleItem(@PathVariable Long id) {
        try {
            saleItemService.deleteSaleItem(id);
            return ResponseEntity.ok("✅ Sale item deleted successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }
}