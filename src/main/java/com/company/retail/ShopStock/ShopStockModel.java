package com.company.retail.ShopStock;

import com.company.retail.product.ProductModel;
import com.company.retail.shop.ShopModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "shop_stock",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"shop_id", "product_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopStockModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shopStockId;

    // ✅ Relationship to Shop
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private ShopModel shop;

    // ✅ Relationship to Product
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductModel product;

    // ✅ Quantity and costs
    private Integer quantityInStock = 0;
    private Double avgLandingCostUSD = 0.0;
    private Double avgLandingCostZWL = 0.0;

    private Double sellingPriceUSD = 0.0;
    private Double sellingPriceZWL = 0.0;

    // ✅ Constructor for quick creation
    public ShopStockModel(ShopModel shop, ProductModel product) {
        this.shop = shop;
        this.product = product;
        this.quantityInStock = 0;
        this.avgLandingCostUSD = 0.0;
        this.avgLandingCostZWL = 0.0;
    }

    @Override
    public String toString() {
        return "ShopStockModel{" +
                "shopStockId=" + shopStockId +
                ", shop=" + (shop != null ? shop.getShopName() : "null") +
                ", product=" + (product != null ? product.getProductName() : "null") +
                ", qty=" + quantityInStock +
                ", costUSD=" + avgLandingCostUSD +
                '}';
    }
}