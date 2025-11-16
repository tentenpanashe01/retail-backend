package com.company.retail.ShopStock;

import com.company.retail.product.ProductModel;
import com.company.retail.product.ProductRepository;
import com.company.retail.shop.ShopModel;
import com.company.retail.shop.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopStockService {

    private final ShopStockRepository shopStockRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    /**
     * ✅ Get all stock records (global view)
     */
    public List<ShopStockModel> getAll() {
        return shopStockRepository.findAll();
    }

    /**
     * ✅ Get stock for a specific shop
     */
    public List<ShopStockModel> getByShop(Long shopId) {
        ShopModel shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        return shopStockRepository.findByShop(shop);
    }

    /**
     * ✅ Get stock for a specific product across all shops
     */
    public List<ShopStockModel> getByProduct(Long productId) {
        ProductModel product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return shopStockRepository.findByProduct(product);
    }

    /**
     * ✅ Adjust stock quantity (used by purchase order or stock transfer)
     */
    public ShopStockModel adjustStock(Long shopId, Long productId, Integer deltaQty, Double newCostUSD, Double newCostZWL) {
        ShopModel shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        ProductModel product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ShopStockModel stock = shopStockRepository.findByShopAndProduct(shop, product)
                .orElse(ShopStockModel.builder()
                        .shop(shop)
                        .product(product)
                        .quantityInStock(0)
                        .avgLandingCostUSD(0.0)
                        .avgLandingCostZWL(0.0)
                        .build());

        int newQty = stock.getQuantityInStock() - deltaQty;
        if (newQty < 0) throw new RuntimeException("Insufficient stock");

        stock.setQuantityInStock(newQty);

        if (newCostUSD != null) stock.setAvgLandingCostUSD(newCostUSD);
        if (newCostZWL != null) stock.setAvgLandingCostZWL(newCostZWL);

        return shopStockRepository.save(stock);
    }
}
