package com.company.retail.stocktransfer;

import com.company.retail.product.ProductModel;
import com.company.retail.shop.ShopModel;
import com.company.retail.user.UserModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Tracks product movement between two shops — includes cost values frozen at time of transfer.
 */
@Entity
@Table(name = "stock_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransferModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    // 🔹 Source Shop
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_shop_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ShopModel fromShop;

    // 🔹 Destination Shop
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_shop_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ShopModel toShop;

    // 🔹 Product being transferred (global product)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductModel product;

    // 🔹 Number of units moved
    @Column(nullable = false)
    private Integer quantity;

    // 🔹 Transfer timestamp
    private LocalDateTime transferDate;

    // 🔹 Current status (PENDING or COMPLETED)
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status;

    // 🔹 User who approved the transfer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UserModel approvedBy;

    // 💰 Cost details at the time of transfer
    @Column(name = "transfer_unit_cost_usd")
    private Double transferUnitCostUSD;

    @Column(name = "transfer_unit_cost_zwl")
    private Double transferUnitCostZWL;

    // 💰 Total value for the quantity transferred
    @Column(name = "transfer_total_cost_usd")
    private Double transferTotalCostUSD;

    @Column(name = "transfer_total_cost_zwl")
    private Double transferTotalCostZWL;

    // 📦 Reference IDs for auditing (optional)
    private String referenceCode;     // e.g. "TXF-2025-001"
    private String remarks;           // optional notes or reason

    public enum Status {
        PENDING,
        COMPLETED,
        CANCELLED
    }

    @PrePersist
    public void onCreate() {
        if (this.transferDate == null) {
            this.transferDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = Status.PENDING;
        }

        // Pre-calculate total cost if unit and quantity provided
        if (this.transferUnitCostUSD != null && this.quantity != null) {
            this.transferTotalCostUSD = this.transferUnitCostUSD * this.quantity;
        }
        if (this.transferUnitCostZWL != null && this.quantity != null) {
            this.transferTotalCostZWL = this.transferUnitCostZWL * this.quantity;
        }
    }
}
