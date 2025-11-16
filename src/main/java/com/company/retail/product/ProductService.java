package com.company.retail.product;

import com.company.retail.ShopStock.ShopStockModel;
import com.company.retail.ShopStock.ShopStockRepository;
import com.company.retail.priceadjustment.PricingAdjustmentModel;
import com.company.retail.priceadjustment.PricingAdjustmentService;
import com.company.retail.shop.ShopModel;
import com.company.retail.shop.ShopRepository;
import com.company.retail.user.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final ShopStockRepository shopStockRepository;
    private final PricingAdjustmentService pricingAdjustmentService;

    /**
     * ✅ Create a new product globally, initialize in all shops
     */
    public ProductModel createProduct(ProductModel product) {
        if (productRepository.existsByProductNameIgnoreCase(product.getProductName())) {
            throw new RuntimeException("Product already exists globally");
        }

        // Save global product info (name, category, selling prices)
        ProductModel savedProduct = productRepository.save(product);

        // Initialize shop-level stock records (each shop starts with zero)
        List<ShopModel> shops = shopRepository.findAll();
        for (ShopModel shop : shops) {
            ShopStockModel stock = ShopStockModel.builder()
                    .shop(shop)
                    .product(savedProduct)
                    .quantityInStock(0)
                    .avgLandingCostUSD(0.0)
                    .avgLandingCostZWL(0.0)
                    .build();
            shopStockRepository.save(stock);
        }

        return savedProduct;
    }

    /**
     * ✅ Update general product details (name, category, unit, reorder)
     * Excludes pricing & cost (handled separately)
     */
    public ProductModel updateProduct(Long id, ProductModel updatedProduct) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setProductName(updatedProduct.getProductName());
        product.setCategory(updatedProduct.getCategory());
        product.setUnit(updatedProduct.getUnit());
        product.setReorderLevel(updatedProduct.getReorderLevel());
        product.setDateUpdated(LocalDateTime.now());

        return productRepository.save(product);
    }

    /**
     * ✅ Update product global selling prices
     * Logs change using PricingAdjustmentService
     */
    public ProductModel updatePricing(Long productId,
                                      Double newSellingUSD,
                                      Double newSellingZWL,
                                      PricingAdjustmentModel.AdjustmentType reason,
                                      UserModel adjustedBy) {

        ProductModel product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Double oldUSD = product.getSellingPriceUSD();
        Double oldZWL = product.getSellingPriceZWL();

        product.setSellingPriceUSD(newSellingUSD);
        product.setSellingPriceZWL(newSellingZWL);
        product.setDateUpdated(LocalDateTime.now());

        ProductModel saved = productRepository.save(product);

        // 🧾 Log pricing adjustment
        pricingAdjustmentService.logGlobalAdjustment(
                productId,
                oldUSD,
                newSellingUSD,
                oldZWL,
                newSellingZWL,
                reason,
                adjustedBy
        );

        return saved;
    }

    /**
     * ✅ Adjust stock for a specific shop (increase or decrease)
     */
    public ShopStockModel adjustShopStock(Long shopId, Long productId, int quantityChange) {
        ShopModel shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        ProductModel product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ShopStockModel shopStock = shopStockRepository.findByShopAndProduct(shop, product)
                .orElseThrow(() -> new RuntimeException("Shop stock record not found"));

        int newQty = shopStock.getQuantityInStock() + quantityChange;
        if (newQty < 0) {
            throw new RuntimeException("Insufficient stock in shop");
        }

        shopStock.setQuantityInStock(newQty);
        return shopStockRepository.save(shopStock);
    }

    /**
     * ✅ Get all global products
     */
    public List<ProductModel> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * ✅ Get single product
     */
    public ProductModel getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    /**
     * ✅ Get shop-specific stock view
     */
    public List<ShopStockModel> getProductsByShop(Long shopId) {
        ShopModel shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        return shopStockRepository.findByShop(shop);
    }

    /**
     * ✅ Delete product (also removes shop-level stock)
     */
    public void deleteProduct(Long id) {
        ProductModel product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Remove all shop stock references
        List<ShopStockModel> stocks = shopStockRepository.findByProduct(product);
        shopStockRepository.deleteAll(stocks);

        productRepository.delete(product);
    }

    /**
     * ✅ Get products below reorder level in a shop
     */
    public List<ShopStockModel> getProductsBelowReorderLevel(Long shopId) {
        ShopModel shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        return shopStockRepository.findByShopAndQuantityInStockLessThanEqual(shop, 0);
    }

    /**
     * ✅ Update shop-specific landing cost (after purchase order completion)
     */
    public void updateLandingCostForShop(ProductModel product, ShopModel shop, double newCostUSD, double newCostZWL) {
        ShopStockModel stock = shopStockRepository.findByShopAndProduct(shop, product)
                .orElseGet(() -> new ShopStockModel(shop, product));

        stock.setAvgLandingCostUSD(newCostUSD);
        stock.setAvgLandingCostZWL(newCostZWL);
        shopStockRepository.save(stock);
    }

    /**
     * ✅ Auto price drop near expiry (optional feature)
     */
    public void autoAdjustPriceForExpiry() {
        LocalDateTime threshold = LocalDateTime.now().plusDays(14);
        List<ProductModel> products = productRepository.findAll();

        for (ProductModel p : products) {
            if (p.getDateUpdated() != null && p.getDateUpdated().isBefore(threshold)) {
                Double discountedUSD = p.getSellingPriceUSD() * 0.8;
                Double discountedZWL = p.getSellingPriceZWL() * 0.8;

                updatePricing(
                        p.getProductId(),
                        discountedUSD,
                        discountedZWL,
                        PricingAdjustmentModel.AdjustmentType.EXPIRY,
                        null
                );
            }
        }
    }
}