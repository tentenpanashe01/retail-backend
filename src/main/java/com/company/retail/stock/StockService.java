package com.company.retail.stock;

import com.company.retail.product.ProductModel;
import com.company.retail.product.ProductRepository;
import com.company.retail.shop.ShopModel;
import com.company.retail.shop.ShopRepository;
import com.company.retail.ShopStock.ShopStockModel;
import com.company.retail.ShopStock.ShopStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final ShopStockRepository shopStockRepository;

    /**
     * ✅ Record any stock movement (IN, OUT, ADJUSTMENT, TRANSFER)
     * Updates per-shop quantity and logs transaction with cost details.
     */
    @Transactional
    public StockModel recordStockMovement(Long productId, Long shopId, Integer qty,
                                          StockModel.TransactionType type,
                                          String reason, String referenceId,
                                          Double unitCostUSD, Double unitCostZWL) {

        ProductModel product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        ShopModel shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // ✅ Fetch or create ShopStock entry for this product at this shop
        ShopStockModel shopStock = shopStockRepository.findByShopAndProduct(shop, product)
                .orElse(ShopStockModel.builder()
                        .shop(shop)
                        .product(product)
                        .quantityInStock(0)
                        .avgLandingCostUSD(0.0)
                        .avgLandingCostZWL(0.0)
                        .build());

        int currentQty = shopStock.getQuantityInStock();
        int newQty = currentQty + qty;

        if (newQty < 0) {
            throw new RuntimeException("Insufficient stock at " + shop.getShopName());
        }

        // ✅ Update per-shop stock quantity
        shopStock.setQuantityInStock(newQty);

        // ✅ If stock is being added, update weighted average cost
        if (qty > 0 && unitCostUSD != null && unitCostZWL != null) {
            double oldValueUSD = shopStock.getAvgLandingCostUSD() * currentQty;
            double newValueUSD = unitCostUSD * qty;
            double oldValueZWL = shopStock.getAvgLandingCostZWL() * currentQty;
            double newValueZWL = unitCostZWL * qty;

            int totalQty = newQty;
            if (totalQty > 0) {
                shopStock.setAvgLandingCostUSD((oldValueUSD + newValueUSD) / totalQty);
                shopStock.setAvgLandingCostZWL((oldValueZWL + newValueZWL) / totalQty);
            }
        }

        shopStockRepository.save(shopStock);

        // ✅ Record movement log for auditing
        StockModel log = new StockModel();
        log.setProduct(product);
        log.setShop(shop);
        log.setQuantityChanged(qty);
        log.setTransactionType(type);
        log.setReason(reason);
        log.setReferenceId(referenceId);
        log.setUnitCostUSD(unitCostUSD);
        log.setUnitCostZWL(unitCostZWL);
        log.setTotalCostUSD(unitCostUSD != null ? unitCostUSD * qty : 0.0);
        log.setTotalCostZWL(unitCostZWL != null ? unitCostZWL * qty : 0.0);
        log.setDate(LocalDateTime.now());

        return stockRepository.save(log);
    }

    /**
     * ✅ Get all stock logs for a given shop
     */
    public List<StockModel> getStockLogsByShop(Long shopId) {
        return stockRepository.findByShop_Id(shopId);
    }

    /**
     * ✅ Get all stock logs for a given product
     */
    public List<StockModel> getStockLogsByProduct(Long productId) {
        return stockRepository.findByProduct_ProductId(productId);
    }

    /**
     * ✅ Delete a stock log record (for admin/audit correction)
     */
    @Transactional
    public void deleteStockLog(Long stockLogId) {
        StockModel log = stockRepository.findById(stockLogId)
                .orElseThrow(() -> new RuntimeException("Stock log not found"));
        stockRepository.delete(log);
    }
}
