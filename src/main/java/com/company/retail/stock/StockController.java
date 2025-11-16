package com.company.retail.stock;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for handling all stock movements across shops.
 * Covers IN, OUT, TRANSFER, and ADJUSTMENT transactions.
 */
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * ✅ Record a stock movement (IN, OUT, ADJUSTMENT, TRANSFER)
     *
     * Example:
     * POST /api/stocks/record?productId=1&shopId=2&qty=10&type=IN&reason=PurchaseOrder&referenceId=PO123&unitCostUSD=10&unitCostZWL=130
     */
    @PostMapping("/record")
    public ResponseEntity<StockModel> recordStockMovement(
            @RequestParam Long productId,
            @RequestParam Long shopId,
            @RequestParam Integer qty,
            @RequestParam StockModel.TransactionType type,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) Double unitCostUSD,
            @RequestParam(required = false) Double unitCostZWL
    ) {
        StockModel record = stockService.recordStockMovement(
                productId, shopId, qty, type, reason, referenceId, unitCostUSD, unitCostZWL
        );
        return ResponseEntity.ok(record);
    }

    /**
     * ✅ Get all stock logs for a specific shop
     * Example: GET /api/stocks/shop/1
     */
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<StockModel>> getStockLogsByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(stockService.getStockLogsByShop(shopId));
    }

    /**
     * ✅ Get all stock logs for a specific product
     * Example: GET /api/stocks/product/5
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockModel>> getStockLogsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(stockService.getStockLogsByProduct(productId));
    }

    /**
     * ✅ Get all stock logs (optional global admin view)
     * Example: GET /api/stocks
     */
    @GetMapping
    public ResponseEntity<List<StockModel>> getAllStockLogs() {
        return ResponseEntity.ok(stockService.getStockLogsByShop(null));
    }

    /**
     * ✅ Delete a specific stock log (for corrections)
     * Example: DELETE /api/stocks/45
     */
    @DeleteMapping("/{stockLogId}")
    public ResponseEntity<Void> deleteStockLog(@PathVariable Long stockLogId) {
        stockService.deleteStockLog(stockLogId);
        return ResponseEntity.noContent().build();
    }
}
