package com.company.retail.saleItem;

import com.company.retail.product.ProductModel;
import com.company.retail.product.ProductRepository;
import com.company.retail.sales.SalesModel;
import com.company.retail.sales.SalesRepository;
import com.company.retail.shop.ShopModel;
import com.company.retail.ShopStock.ShopStockModel;
import com.company.retail.ShopStock.ShopStockRepository;
import com.company.retail.stock.StockModel;
import com.company.retail.stock.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SaleItemService {

    private final SaleItemRepository saleItemRepository;
    private final SalesRepository salesRepository;
    private final ProductRepository productRepository;
    private final ShopStockRepository shopStockRepository;
    private final StockRepository stockRepository;

    /**
     * ✅ Get all sale items
     */
    public List<SaleItemModel> getAllSaleItems() {
        return saleItemRepository.findAll();
    }

    /**
     * ✅ Get a single sale item
     */
    public Optional<SaleItemModel> getSaleItemById(Long id) {
        return saleItemRepository.findById(id);
    }

    /**
     * ✅ Get all items for a given sale
     */
    public List<SaleItemModel> getItemsBySale(Long saleId) {
        return saleItemRepository.findBySale_SaleId(saleId);
    }

    /**
     * ✅ Create a sale item (deduct stock and record logs)
     */
    @Transactional
    public SaleItemModel createSaleItem(SaleItemModel item) {
        SalesModel sale = salesRepository.findById(item.getSale().getSaleId())
                .orElseThrow(() -> new RuntimeException("Sale not found"));

        ProductModel product = productRepository.findById(item.getProduct().getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ShopModel shop = sale.getShop();
        ShopStockModel shopStock = shopStockRepository.findByShopAndProduct(shop, product)
                .orElseThrow(() -> new RuntimeException("No stock record for " + product.getProductName()));

        if (shopStock.getQuantityInStock() < item.getQuantity()) {
            throw new RuntimeException("Insufficient stock for " + product.getProductName());
        }

        // 🔹 Calculate financials
        double sellUSD = product.getSellingPriceUSD();
        double sellZWL = product.getSellingPriceZWL();
        double costUSD = shopStock.getAvgLandingCostUSD();
        double costZWL = shopStock.getAvgLandingCostZWL();

        item.setSellingPriceUSD(sellUSD);
        item.setSellingPriceZWL(sellZWL);
        item.setCostPriceUSD(costUSD);
        item.setCostPriceZWL(costZWL);

        double totalUSD = sellUSD * item.getQuantity();
        double totalZWL = sellZWL * item.getQuantity();
        item.setTotalUSD(totalUSD);
        item.setTotalZWL(totalZWL);

        item.setProfitUSD((sellUSD - costUSD) * item.getQuantity());
        item.setProfitZWL((sellZWL - costZWL) * item.getQuantity());

        item.setSale(sale);

        // 🔹 Deduct stock
        shopStock.setQuantityInStock(shopStock.getQuantityInStock() - item.getQuantity());
        shopStockRepository.save(shopStock);

        // 🔹 Save item
        SaleItemModel savedItem = saleItemRepository.save(item);

        // 🔹 Log transaction
        StockModel log = StockModel.builder()
                .product(product)
                .shop(shop)
                .quantityChanged(-item.getQuantity())
                .transactionType(StockModel.TransactionType.OUT)
                .reason("SaleItem created for Sale #" + sale.getSaleId())
                .referenceId("SALEITEM-" + savedItem.getSaleItemId())
                .unitCostUSD(costUSD)
                .unitCostZWL(costZWL)
                .totalCostUSD(costUSD * item.getQuantity())
                .totalCostZWL(costZWL * item.getQuantity())
                .date(LocalDateTime.now())
                .build();

        stockRepository.save(log);
        return savedItem;
    }

    /**
     * ✅ Update a sale item (adjust stock if quantity changes)
     */
    @Transactional
    public SaleItemModel updateSaleItem(Long id, SaleItemModel updatedItem) {
        SaleItemModel existing = saleItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale item not found"));

        SalesModel sale = existing.getSale();
        ProductModel product = existing.getProduct();
        ShopModel shop = sale.getShop();

        ShopStockModel shopStock = shopStockRepository.findByShopAndProduct(shop, product)
                .orElseThrow(() -> new RuntimeException("No stock record for " + product.getProductName()));

        int diff = updatedItem.getQuantity() - existing.getQuantity();

        if (diff > 0 && shopStock.getQuantityInStock() < diff) {
            throw new RuntimeException("Not enough stock to increase sale quantity for " + product.getProductName());
        }

        // 🔹 Adjust stock
        shopStock.setQuantityInStock(shopStock.getQuantityInStock() - diff);
        shopStockRepository.save(shopStock);

        // 🔹 Update fields
        existing.setQuantity(updatedItem.getQuantity());
        existing.setSellingPriceUSD(updatedItem.getSellingPriceUSD());
        existing.setSellingPriceZWL(updatedItem.getSellingPriceZWL());
        existing.setTotalUSD(updatedItem.getSellingPriceUSD() * updatedItem.getQuantity());
        existing.setTotalZWL(updatedItem.getSellingPriceZWL() * updatedItem.getQuantity());
        existing.setProfitUSD((updatedItem.getSellingPriceUSD() - existing.getCostPriceUSD()) * updatedItem.getQuantity());
        existing.setProfitZWL((updatedItem.getSellingPriceZWL() - existing.getCostPriceZWL()) * updatedItem.getQuantity());

        SaleItemModel saved = saleItemRepository.save(existing);

        // 🔹 Log adjustment
        if (diff != 0) {
            StockModel log = StockModel.builder()
                    .product(product)
                    .shop(shop)
                    .quantityChanged(-diff)
                    .transactionType(StockModel.TransactionType.ADJUSTMENT)
                    .reason("SaleItem update for Sale #" + sale.getSaleId())
                    .referenceId("SALEITEM-UPD-" + saved.getSaleItemId())
                    .unitCostUSD(existing.getCostPriceUSD())
                    .unitCostZWL(existing.getCostPriceZWL())
                    .totalCostUSD(existing.getCostPriceUSD() * diff)
                    .totalCostZWL(existing.getCostPriceZWL() * diff)
                    .date(LocalDateTime.now())
                    .build();

            stockRepository.save(log);
        }

        return saved;
    }

    /**
     * ✅ Delete a sale item (restore stock)
     */
    @Transactional
    public void deleteSaleItem(Long id) {
        SaleItemModel item = saleItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SaleItem not found"));

        SalesModel sale = item.getSale();
        ProductModel product = item.getProduct();
        ShopModel shop = sale.getShop();

        ShopStockModel shopStock = shopStockRepository.findByShopAndProduct(shop, product)
                .orElseThrow(() -> new RuntimeException("No stock record for " + product.getProductName()));

        // 🔹 Restore stock
        shopStock.setQuantityInStock(shopStock.getQuantityInStock() + item.getQuantity());
        shopStockRepository.save(shopStock);

        // 🔹 Log restoration
        StockModel log = StockModel.builder()
                .product(product)
                .shop(shop)
                .quantityChanged(item.getQuantity())
                .transactionType(StockModel.TransactionType.ADJUSTMENT)
                .reason("SaleItem deleted (Sale #" + sale.getSaleId() + ")")
                .referenceId("SALEITEM-DEL-" + item.getSaleItemId())
                .unitCostUSD(item.getCostPriceUSD())
                .unitCostZWL(item.getCostPriceZWL())
                .totalCostUSD(item.getCostPriceUSD() * item.getQuantity())
                .totalCostZWL(item.getCostPriceZWL() * item.getQuantity())
                .date(LocalDateTime.now())
                .build();

        stockRepository.save(log);

        saleItemRepository.delete(item);
    }
}