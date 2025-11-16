package com.company.retail.ShopStock;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/shop-stock")
@RequiredArgsConstructor
public class ShopStockController {

    private final ShopStockService shopStockService;

    /** ✅ View all stock (Admins and SuperAdmins only) */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @GetMapping
    public ResponseEntity<List<ShopStockModel>> getAll() {
        return ResponseEntity.ok(shopStockService.getAll());
    }

    /** ✅ View stock by shop (accessible to any authenticated user) */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<ShopStockModel>> getByShop(@PathVariable Long shopId) {
        List<ShopStockModel> stocks = shopStockService.getByShop(shopId);
        if (stocks == null) {
            stocks = new ArrayList<>();
        }
        return ResponseEntity.ok(stocks);
    }
    /** ✅ View stock by product (accessible to any authenticated user) */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ShopStockModel>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(shopStockService.getByProduct(productId));
    }

    /**
     * ✅ Adjust stock quantity
     * 🔒 Restricted to ADMIN and SUPERADMIN only
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @PatchMapping("/adjust")
    public ResponseEntity<?> adjustStock(
            @RequestParam Long shopId,
            @RequestParam Long productId,
            @RequestParam Integer deltaQty,
            @RequestParam(required = false) Double costUSD,
            @RequestParam(required = false) Double costZWL
    ) {
        try {
            ShopStockModel updated = shopStockService.adjustStock(shopId, productId, deltaQty, costUSD, costZWL);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ Failed to adjust stock: " + e.getMessage());
        }
    }
}