package com.company.retail.sales;

import com.company.retail.product.ProductModel;
import com.company.retail.product.ProductRepository;
import com.company.retail.saleItem.SaleItemModel;
import com.company.retail.saleItem.SaleItemRepository;
import com.company.retail.shop.ShopModel;
import com.company.retail.shop.ShopRepository;
import com.company.retail.ShopStock.ShopStockModel;
import com.company.retail.ShopStock.ShopStockRepository;
import com.company.retail.stock.StockModel;
import com.company.retail.stock.StockRepository;
import com.company.retail.user.UserModel;
import com.company.retail.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final SalesRepository salesRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final ShopStockRepository shopStockRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;

    @Transactional
    public SalesModel createSale(SalesModel saleRequest, Long shopId, Long cashierId) {

        // Fetch Shop and Cashier
        ShopModel shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        UserModel cashier = userRepository.findById(cashierId)
                .orElseThrow(() -> new RuntimeException("Cashier not found"));

        // Prepare sale header
        SalesModel sale = new SalesModel();
        sale.setShop(shop);
        sale.setCashier(cashier);
        sale.setSaleDate(LocalDateTime.now());

        double totalUSD = 0.0;
        double totalZWL = 0.0;

        // Save sale header FIRST
        SalesModel savedSale = salesRepository.save(sale);

        // Process each item
        for (SaleItemModel item : saleRequest.getSaleItems()) {

            ProductModel product = productRepository.findById(item.getProduct().getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            ShopStockModel shopStock = shopStockRepository.findByShopAndProduct(shop, product)
                    .orElseThrow(() -> new RuntimeException("No stock for product: " + product.getProductName()));

            // Ensure stock is enough
            if (shopStock.getQuantityInStock() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + product.getProductName());
            }

            // Reduce stock
            shopStock.setQuantityInStock(shopStock.getQuantityInStock() - item.getQuantity());
            shopStockRepository.save(shopStock);

            // Capture prices
            double costUSD = shopStock.getAvgLandingCostUSD();
            double costZWL = shopStock.getAvgLandingCostZWL();
            double sellUSD = product.getSellingPriceUSD();
            double sellZWL = product.getSellingPriceZWL();

            // Attach sale reference (IMPORTANT FIX)
            item.setSale(savedSale);
            item.setProduct(product);
            item.setSellingPriceUSD(sellUSD);
            item.setSellingPriceZWL(sellZWL);
            item.setCostPriceUSD(costUSD);
            item.setCostPriceZWL(costZWL);

            // Line totals
            double lineTotalUSD = sellUSD * item.getQuantity();
            double lineTotalZWL = sellZWL * item.getQuantity();
            item.setTotalUSD(lineTotalUSD);
            item.setTotalZWL(lineTotalZWL);

            // Profit
            item.setProfitUSD((sellUSD - costUSD) * item.getQuantity());
            item.setProfitZWL((sellZWL - costZWL) * item.getQuantity());

            // Add to sale totals
            totalUSD += lineTotalUSD;
            totalZWL += lineTotalZWL;

            // Save item (AFTER sale is set)
            saleItemRepository.save(item);

            // Log stock movement
            stockRepository.save(StockModel.builder()
                    .product(product)
                    .shop(shop)
                    .quantityChanged(-item.getQuantity())
                    .transactionType(StockModel.TransactionType.OUT)
                    .reason("Sale #" + savedSale.getSaleId())
                    .referenceId("SALE-" + savedSale.getSaleId())
                    .unitCostUSD(costUSD)
                    .unitCostZWL(costZWL)
                    .totalCostUSD(costUSD * item.getQuantity())
                    .totalCostZWL(costZWL * item.getQuantity())
                    .date(LocalDateTime.now())
                    .build());
        }

        // Update totals
        savedSale.setTotalAmountUSD(totalUSD);
        savedSale.setTotalAmountZWL(totalZWL);

        return salesRepository.save(savedSale);
    }

    public List<SalesModel> getAllSales() {
        return salesRepository.findAll();
    }

    public SalesModel getSaleById(Long id) {
        return salesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    public List<SalesModel> getSalesByShop(Long shopId) {
        return salesRepository.findByShop_Id(shopId);
    }

    public List<SalesModel> getSalesBetween(LocalDateTime start, LocalDateTime end) {
        return salesRepository.findBySaleDateBetween(start, end);
    }

    public void deleteSale(Long id) {
        salesRepository.deleteById(id);
    }

    public List<SalesModel> getSalesForShopOnDate(Long shopId, LocalDateTime start, LocalDateTime end) {
        return salesRepository.findByShop_Id(shopId).stream()
                .filter(s -> !s.getSaleDate().isBefore(start) && !s.getSaleDate().isAfter(end))
                .toList();
    }

    public List<SalesModel> getSalesForUserOnDate(Long userId, LocalDateTime start, LocalDateTime end) {
        return salesRepository.findAll().stream()
                .filter(s -> s.getCashier().getUserId().equals(userId)
                        && !s.getSaleDate().isBefore(start)
                        && !s.getSaleDate().isAfter(end))
                .toList();
    }
}