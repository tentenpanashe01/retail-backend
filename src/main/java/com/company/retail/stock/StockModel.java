package com.company.retail.stock;

import com.company.retail.product.ProductModel;
import com.company.retail.shop.ShopModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductModel product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ShopModel shop;

    private Integer quantityChanged;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // IN, OUT, TRANSFER_IN, TRANSFER_OUT, ADJUSTMENT

    private String reason;
    private String referenceId;
    private LocalDateTime date;

    // — Landing cost value per unit at the time of movement
    private Double unitCostUSD;
    private Double unitCostZWL;

    //  — Total cost of the stock movement for reporting
    private Double totalCostUSD;
    private Double totalCostZWL;

    public enum TransactionType {
        IN, OUT, TRANSFER_IN, TRANSFER_OUT, ADJUSTMENT
    }

    @PrePersist
    public void prePersist() {
        this.date = LocalDateTime.now();
    }
}
