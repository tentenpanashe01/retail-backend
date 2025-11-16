package com.company.retail.stocktransfer;

import com.company.retail.ShopStock.ShopStockModel;
import com.company.retail.ShopStock.ShopStockRepository;
import com.company.retail.product.ProductModel;
import com.company.retail.product.ProductRepository;
import com.company.retail.shop.ShopModel;
import com.company.retail.shop.ShopRepository;
import com.company.retail.user.UserModel;
import com.company.retail.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final ShopStockRepository shopStockRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * ✅ Create a pending stock transfer request
     */
    public StockTransferModel createTransfer(Long fromShopId, Long toShopId, Long productId, Integer quantity) {
        if (fromShopId.equals(toShopId)) {
            throw new RuntimeException("Source and destination shops cannot be the same.");
        }

        ShopModel fromShop = shopRepository.findById(fromShopId)
                .orElseThrow(() -> new RuntimeException("Source shop not found"));
        ShopModel toShop = shopRepository.findById(toShopId)
                .orElseThrow(() -> new RuntimeException("Destination shop not found"));
        ProductModel product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // ✅ Check stock in source shop
        ShopStockModel fromStock = shopStockRepository.findByShopAndProduct(fromShop, product)
                .orElseThrow(() -> new RuntimeException("No stock record for product in source shop"));

        if (fromStock.getQuantityInStock() < quantity) {
            throw new RuntimeException("Insufficient stock in source shop.");
        }

        // ✅ Freeze cost at transfer time
        Double unitCostUSD = fromStock.getAvgLandingCostUSD();
        Double unitCostZWL = fromStock.getAvgLandingCostZWL();

        // ✅ Generate unique reference code
        String reference = "TXF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // ✅ Build transfer record
        StockTransferModel transfer = StockTransferModel.builder()
                .fromShop(fromShop)
                .toShop(toShop)
                .product(product)
                .quantity(quantity)
                .transferUnitCostUSD(unitCostUSD)
                .transferUnitCostZWL(unitCostZWL)
                .transferTotalCostUSD(unitCostUSD * quantity)
                .transferTotalCostZWL(unitCostZWL * quantity)
                .referenceCode(reference)
                .status(StockTransferModel.Status.PENDING)
                .transferDate(LocalDateTime.now())
                .remarks("Transfer initiated from " + fromShop.getShopName() + " to " + toShop.getShopName())
                .build();

        return stockTransferRepository.save(transfer);
    }

    /**
     * ✅ Approve and complete a stock transfer
     */
    @Transactional
    public StockTransferModel completeTransfer(Long transferId, Long approverId) {
        StockTransferModel transfer = stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        if (transfer.getStatus() == StockTransferModel.Status.COMPLETED) {
            throw new RuntimeException("This transfer is already completed.");
        }

        ProductModel product = transfer.getProduct();
        ShopModel fromShop = transfer.getFromShop();
        ShopModel toShop = transfer.getToShop();
        int qty = transfer.getQuantity();

        // ✅ Get per-shop stock
        ShopStockModel fromStock = shopStockRepository.findByShopAndProduct(fromShop, product)
                .orElseThrow(() -> new RuntimeException("No stock found in source shop."));
        ShopStockModel toStock = shopStockRepository.findByShopAndProduct(toShop, product)
                .orElse(ShopStockModel.builder()
                        .shop(toShop)
                        .product(product)
                        .quantityInStock(0)
                        .avgLandingCostUSD(0.0)
                        .avgLandingCostZWL(0.0)
                        .build());

        if (fromStock.getQuantityInStock() < qty) {
            throw new RuntimeException("Insufficient stock to transfer.");
        }

        // ✅ Adjust stock quantities
        fromStock.setQuantityInStock(fromStock.getQuantityInStock() - qty);
        toStock.setQuantityInStock(toStock.getQuantityInStock() + qty);

        // ✅ Copy cost values
        toStock.setAvgLandingCostUSD(fromStock.getAvgLandingCostUSD());
        toStock.setAvgLandingCostZWL(fromStock.getAvgLandingCostZWL());

        // ✅ Save updates
        shopStockRepository.save(fromStock);
        shopStockRepository.save(toStock);

        // ✅ Update transfer record
        UserModel approver = userRepository.findById(approverId)
                .orElse(null);
        transfer.setStatus(StockTransferModel.Status.COMPLETED);
        transfer.setApprovedBy(approver);
        transfer.setRemarks("Transfer approved by " + (approver != null ? approver.getFullName() : "System"));

        return stockTransferRepository.save(transfer);
    }

    /**
     * ✅ Get all stock transfers
     */
    public List<StockTransferModel> getAllTransfers() {
        return stockTransferRepository.findAll();
    }

    /**
     * ✅ Get transfers related to a specific shop
     */
    public List<StockTransferModel> getTransfersByShop(Long shopId) {
        ShopModel shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        return stockTransferRepository.findByFromShopOrToShop(shop, shop);
    }

    /**
     * ✅ Delete a transfer record (if still pending)
     */
    public void deleteTransfer(Long id) {
        StockTransferModel transfer = stockTransferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        if (transfer.getStatus() == StockTransferModel.Status.COMPLETED) {
            throw new RuntimeException("Cannot delete a completed transfer.");
        }

        stockTransferRepository.deleteById(id);
    }

    /**
     * ✅ Utility: Fetch transfer by ID
     */
    public StockTransferModel getById(Long id) {
        return stockTransferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));
    }
}
