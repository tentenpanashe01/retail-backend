package com.company.retail.priceadjustment;

import com.company.retail.ShopStock.ShopStockModel;
import com.company.retail.ShopStock.ShopStockRepository;
import com.company.retail.product.ProductModel;
import com.company.retail.product.ProductRepository;
import com.company.retail.shop.ShopModel;
import com.company.retail.shop.ShopRepository;
import com.company.retail.user.UserModel;
import com.company.retail.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pricing-adjustments")
@RequiredArgsConstructor
public class PricingAdjustmentController {

    private final PricingAdjustmentService pricingAdjustmentService;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final ShopStockRepository shopStockRepository;
    private final UserRepository userRepository;

    // ============================================================
    // 📘 GET METHODS — accessible to all authenticated roles
    // ============================================================

    @PreAuthorize("hasAnyRole('CASHIER','SUPERVISOR','ADMIN','SUPERADMIN')")
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<PricingAdjustmentModel>> getAdjustmentsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(pricingAdjustmentService.getAdjustmentsByProduct(productId));
    }

    @PreAuthorize("hasAnyRole('CASHIER','SUPERVISOR','ADMIN','SUPERADMIN')")
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<PricingAdjustmentModel>> getAdjustmentsByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(pricingAdjustmentService.getAdjustmentsByShop(shopId));
    }

    @PreAuthorize("hasAnyRole('CASHIER','SUPERVISOR','ADMIN','SUPERADMIN')")
    @GetMapping
    public ResponseEntity<List<PricingAdjustmentModel>> getAllAdjustments() {
        return ResponseEntity.ok(pricingAdjustmentService.getAllAdjustments());
    }

    // ============================================================
    // 🧠 HELPER — get logged-in user safely from Security Context
    // ============================================================

    private UserModel getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user found.");
        }

        Object principal = auth.getPrincipal();

        // Case 1️⃣ — Username is stored directly (e.g., String)
        if (principal instanceof String username) {
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        }

        // Case 2️⃣ — JWT stores the whole UserModel object as principal
        if (principal instanceof UserModel userModel) {
            Long userId = userModel.getUserId();
            if (userId != null) {
                return userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            }
            // fallback: try username
            return userRepository.findByUsername(userModel.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + userModel.getUsername()));
        }

        // Case 3️⃣ — Other custom UserDetails implementation
        try {
            String username = auth.getName();
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        } catch (Exception e) {
            throw new RuntimeException("Unable to resolve authenticated user principal.");
        }
    }

    private void ensureAdmin(UserModel user) {
        if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new AccessDeniedException("User roles not found.");
        }

        boolean isAdmin = user.getRoles().contains(UserModel.Role.ROLE_ADMIN)
                || user.getRoles().contains(UserModel.Role.ROLE_SUPERADMIN);

        if (!isAdmin) {
            throw new AccessDeniedException("Only Admins or SuperAdmins can adjust prices.");
        }
    }

    // ============================================================
    // 🌍 GLOBAL PRICE UPDATE — Admins only
    // ============================================================

    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @PutMapping("/global/{productId}/update-price")
    public ResponseEntity<ProductModel> updateGlobalPrice(
            @PathVariable Long productId,
            @RequestBody Map<String, Object> body) {

        UserModel currentUser = getCurrentUser();
        ensureAdmin(currentUser);

        Double newSellingUSD = getDouble(body, "newSellingUSD");
        Double newSellingZWL = getDouble(body, "newSellingZWL");
        String reasonStr = (String) body.getOrDefault("reason", "MANUAL");

        PricingAdjustmentModel.AdjustmentType reason =
                PricingAdjustmentModel.AdjustmentType.valueOf(reasonStr.toUpperCase());

        ProductModel product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("❌ Product not found."));

        Double oldUSD = product.getSellingPriceUSD();
        Double oldZWL = product.getSellingPriceZWL();

        if (newSellingUSD != null) product.setSellingPriceUSD(newSellingUSD);
        if (newSellingZWL != null) product.setSellingPriceZWL(newSellingZWL);
        productRepository.save(product);

        pricingAdjustmentService.logGlobalAdjustment(
                productId, oldUSD, newSellingUSD, oldZWL, newSellingZWL, reason, currentUser
        );

        return ResponseEntity.ok(product);
    }

    // ============================================================
    // 🏪 SHOP-SPECIFIC PRICE UPDATE — Admins only
    // ============================================================

    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    @PutMapping("/shop/{shopId}/product/{productId}/update-price")
    public ResponseEntity<ShopStockModel> updateShopPrice(
            @PathVariable Long shopId,
            @PathVariable Long productId,
            @RequestBody Map<String, Object> body) {

        UserModel currentUser = getCurrentUser();
        ensureAdmin(currentUser);

        Double newSellingUSD = getDouble(body, "newSellingUSD");
        Double newSellingZWL = getDouble(body, "newSellingZWL");
        String reasonStr = (String) body.getOrDefault("reason", "MANUAL");

        PricingAdjustmentModel.AdjustmentType reason =
                PricingAdjustmentModel.AdjustmentType.valueOf(reasonStr.toUpperCase());

        ShopModel shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("❌ Shop not found."));
        ProductModel product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("❌ Product not found."));
        ShopStockModel shopStock = shopStockRepository.findByShop_IdAndProduct_ProductId(shopId, productId)
                .orElseThrow(() -> new RuntimeException("❌ Shop stock not found for this product."));

        Double oldUSD = shopStock.getSellingPriceUSD();
        Double oldZWL = shopStock.getSellingPriceZWL();

        if (newSellingUSD != null) shopStock.setSellingPriceUSD(newSellingUSD);
        if (newSellingZWL != null) shopStock.setSellingPriceZWL(newSellingZWL);
        shopStockRepository.save(shopStock);

        pricingAdjustmentService.logShopAdjustment(
                productId, shopId, oldUSD, newSellingUSD, oldZWL, newSellingZWL, reason, currentUser
        );

        return ResponseEntity.ok(shopStock);
    }

    // ============================================================
    // 🔧 HELPER METHODS
    // ============================================================

    private Double getDouble(Map<String, Object> body, String key) {
        try {
            return body.containsKey(key) && body.get(key) != null
                    ? Double.valueOf(body.get(key).toString())
                    : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Long getLong(Map<String, Object> body, String key) {
        try {
            return body.containsKey(key) && body.get(key) != null
                    ? Long.valueOf(body.get(key).toString())
                    : null;
        } catch (Exception e) {
            return null;
        }
    }
}