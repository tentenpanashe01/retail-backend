package com.company.retail.saleItem;

import com.company.retail.product.ProductModel;
import com.company.retail.sales.SalesModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sale_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SaleItemModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long saleItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @JsonIgnoreProperties("saleItems")
    private SalesModel sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"shopStocks"})
    private ProductModel product;

    private Integer quantity;

    private Double sellingPriceUSD;
    private Double sellingPriceZWL;
    private Double costPriceUSD;
    private Double costPriceZWL;

    private Double totalUSD;
    private Double totalZWL;
    private Double profitUSD;
    private Double profitZWL;

    @PrePersist
    @PreUpdate
    public void calculateTotals() {
        if (sellingPriceUSD != null && costPriceUSD != null && quantity != null) {
            totalUSD = sellingPriceUSD * quantity;
            profitUSD = (sellingPriceUSD - costPriceUSD) * quantity;
        }
        if (sellingPriceZWL != null && costPriceZWL != null && quantity != null) {
            totalZWL = sellingPriceZWL * quantity;
            profitZWL = (sellingPriceZWL - costPriceZWL) * quantity;
        }
    }
}