package com.company.retail.product;

import com.company.retail.ShopStock.ShopStockModel;
import com.company.retail.priceadjustment.PricingAdjustmentModel;
import com.company.retail.user.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller for Product & Shop Stock Management
 * Secured for Admins and SuperAdmins
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor

public class ProductController {

    private final ProductService productService;

    /**
     * ✅ View all products (any logged-in user can view)
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<ProductModel>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * ✅ View product by ID (any logged-in user)
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.getProductById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("❌ " + e.getMessage());
        }
    }

    /**
     * ✅ Create a new product
     * Restricted to Admin or SuperAdmin
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductModel product) {
        try {
            ProductModel saved = productService.createProduct(product);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }

    /**
     * ✅ Update product details
     * Restricted to Admin or SuperAdmin
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductModel updatedProduct) {
        try {
            ProductModel saved = productService.updateProduct(id, updatedProduct);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }

    /**
     * ✅ Update global selling prices and log adjustment
     * Only Admin or SuperAdmin can perform this
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @PatchMapping("/{productId}/pricing")
    public ResponseEntity<?> updatePricing(
            @PathVariable Long productId,
            @RequestParam Double newSellingUSD,
            @RequestParam Double newSellingZWL,
            @RequestParam PricingAdjustmentModel.AdjustmentType reason,
            @RequestParam(required = false) Long userId
    ) {
        try {
            UserModel adjustedBy = userId != null ? new UserModel(userId) : null;
            ProductModel updated = productService.updatePricing(
                    productId, newSellingUSD, newSellingZWL, reason, adjustedBy
            );
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }

    /**
     * ✅ Adjust stock for a specific shop (manual correction)
     * Only Admin or SuperAdmin can do this
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @PatchMapping("/{productId}/stock")
    public ResponseEntity<?> adjustStock(
            @PathVariable Long productId,
            @RequestParam Long shopId,
            @RequestParam int quantityChange
    ) {
        try {
            ShopStockModel updatedStock = productService.adjustShopStock(shopId, productId, quantityChange);
            return ResponseEntity.ok(updatedStock);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("❌ " + e.getMessage());
        }
    }

    /**
     * ✅ Get shop-specific stock listing
     * Any logged-in user can view
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<?> getProductsByShop(@PathVariable Long shopId) {
        try {
            List<ShopStockModel> stocks = productService.getProductsByShop(shopId);
            return ResponseEntity.ok(stocks);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("❌ " + e.getMessage());
        }
    }

    /**
     * ✅ Get products below reorder level (for alerts)
     * Any logged-in user can view
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/reorder/{shopId}")
    public ResponseEntity<?> getBelowReorder(@PathVariable Long shopId) {
        try {
            List<ShopStockModel> lowStock = productService.getProductsBelowReorderLevel(shopId);
            return ResponseEntity.ok(lowStock);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("❌ " + e.getMessage());
        }
    }

    /**
     * ✅ Delete a product
     * Only Admin or SuperAdmin can delete
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok("✅ Product deleted successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("❌ " + e.getMessage());
        }
    }
}
