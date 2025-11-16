package com.company.retail.stocktransfer;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing stock transfers between shops.
 * Handles creation, approval, retrieval, and deletion.
 */
@RestController
@RequestMapping("/api/stock-transfers")
@RequiredArgsConstructor
public class StockTransferController {

    private final StockTransferService stockTransferService;

    /**
     * ✅ Create a pending stock transfer request
     * Example: POST /api/stock-transfers?fromShopId=1&toShopId=2&productId=5&quantity=10
     */
    @PostMapping
    public ResponseEntity<StockTransferModel> createTransfer(
            @RequestParam Long fromShopId,
            @RequestParam Long toShopId,
            @RequestParam Long productId,
            @RequestParam Integer quantity
    ) {
        StockTransferModel created = stockTransferService.createTransfer(fromShopId, toShopId, productId, quantity);
        return ResponseEntity.ok(created);
    }

    /**
     * ✅ Approve and complete a transfer (mark as completed)
     * Example: PUT /api/stock-transfers/{id}/complete?approverId=3
     */
    @PutMapping("/{transferId}/complete")
    public ResponseEntity<StockTransferModel> completeTransfer(
            @PathVariable Long transferId,
            @RequestParam(required = false) Long approverId
    ) {
        StockTransferModel updated = stockTransferService.completeTransfer(transferId, approverId);
        return ResponseEntity.ok(updated);
    }

    /**
     * ✅ Get all stock transfers
     * Example: GET /api/stock-transfers
     */
    @GetMapping
    public ResponseEntity<List<StockTransferModel>> getAllTransfers() {
        return ResponseEntity.ok(stockTransferService.getAllTransfers());
    }

    /**
     * ✅ Get all transfers related to a specific shop (as source or destination)
     * Example: GET /api/stock-transfers/shop/1
     */
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<StockTransferModel>> getTransfersByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(stockTransferService.getTransfersByShop(shopId));
    }

    /**
     * ✅ Get a specific transfer by ID
     * Example: GET /api/stock-transfers/15
     */
    @GetMapping("/{transferId}")
    public ResponseEntity<StockTransferModel> getTransferById(@PathVariable Long transferId) {
        return ResponseEntity.ok(stockTransferService.getById(transferId));
    }

    /**
     * ✅ Delete a transfer (only if still pending)
     * Example: DELETE /api/stock-transfers/15
     */
    @DeleteMapping("/{transferId}")
    public ResponseEntity<Void> deleteTransfer(@PathVariable Long transferId) {
        stockTransferService.deleteTransfer(transferId);
        return ResponseEntity.noContent().build();
    }
}
