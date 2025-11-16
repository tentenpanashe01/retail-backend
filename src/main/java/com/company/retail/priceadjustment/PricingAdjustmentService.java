package com.company.retail.priceadjustment;

import com.company.retail.ShopStock.ShopStockModel;
import com.company.retail.ShopStock.ShopStockRepository;
import com.company.retail.product.ProductModel;
import com.company.retail.product.ProductRepository;
import com.company.retail.shop.ShopModel;
import com.company.retail.shop.ShopRepository;
import com.company.retail.user.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PricingAdjustmentService {

    private final PricingAdjustmentRepository pricingAdjustmentRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final ShopStockRepository shopStockRepository;

    /**
     * 🔹 Log global (all-shops) price adjustment.
     * Used when a price change applies system-wide to all shops.
     */
    public PricingAdjustmentModel logGlobalAdjustment(
            Long productId,
            Double oldUSD,
            Double newUSD,
            Double oldZWL,
            Double newZWL,
            PricingAdjustmentModel.AdjustmentType reason,
            UserModel user
    ) {
        ProductModel product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("❌ Product not found."));

        // ✅ Apply new global prices to ProductModel
        if (newUSD != null) product.setSellingPriceUSD(newUSD);
        if (newZWL != null) product.setSellingPriceZWL(newZWL);
        productRepository.save(product);

        // ✅ Log global price adjustment (no shop linked)
        PricingAdjustmentModel adjustment = PricingAdjustmentModel.builder()
                .product(product)
                .shop(null) // ✅ Explicitly set shop as null for global changes
                .adjustedBy(user)
                .oldPriceUSD(oldUSD)
                .newPriceUSD(newUSD)
                .oldPriceZWL(oldZWL)
                .newPriceZWL(newZWL)
                .reason(reason)
                .build();

        return pricingAdjustmentRepository.save(adjustment);
    }

    /**
     * 🔹 Log shop-specific price adjustment.
     * Used when a price change applies to one shop only.
     */
    @Transactional
    public void logShopAdjustment(Long productId, Long shopId,
                                  Double oldUSD, Double newUSD,
                                  Double oldZWL, Double newZWL,
                                  PricingAdjustmentModel.AdjustmentType reason,
                                  UserModel adjustedBy) {

        System.out.println("🧩 logShopAdjustment called for shopId=" + shopId + ", productId=" + productId);

        // --- Fetch entities safely
        ProductModel product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("❌ Product not found for adjustment."));

        ShopModel shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("❌ Shop not found with ID: " + shopId));

        // --- Build adjustment record
        PricingAdjustmentModel adj = new PricingAdjustmentModel();
        adj.setProduct(product);
        adj.setShop(shop);
        adj.setAdjustedBy(adjustedBy);
        adj.setOldPriceUSD(oldUSD);
        adj.setNewPriceUSD(newUSD);
        adj.setOldPriceZWL(oldZWL);
        adj.setNewPriceZWL(newZWL);
        adj.setReason(reason);
        adj.setAdjustmentDate(LocalDateTime.now());

        pricingAdjustmentRepository.save(adj);

        System.out.println("✅ Logged shop price adjustment for " + shop.getShopName() + " -> " + product.getProductName());
    }
    /**
     * 🔹 Get all price adjustments for a given product.
     */
    public List<PricingAdjustmentModel> getAdjustmentsByProduct(Long productId) {
        return pricingAdjustmentRepository.findByProduct_ProductId(productId);
    }

    /**
     * 🔹 Get all price adjustments for a given shop.
     */
    public List<PricingAdjustmentModel> getAdjustmentsByShop(Long shopId) {
        return pricingAdjustmentRepository.findByShop_Id(shopId);
    }

    /**
     * 🔹 Get all price adjustments (system-wide).
     */
    public List<PricingAdjustmentModel> getAllAdjustments() {
        return pricingAdjustmentRepository.findAll();
    }
}