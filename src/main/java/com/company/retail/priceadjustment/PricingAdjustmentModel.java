package com.company.retail.priceadjustment;

import com.company.retail.product.ProductModel;
import com.company.retail.shop.ShopModel;
import com.company.retail.user.UserModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_adjustments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingAdjustmentModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adjustmentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductModel product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shop_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ShopModel shop;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "adjusted_by", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UserModel adjustedBy;

    private Double oldPriceUSD;
    private Double newPriceUSD;
    private Double oldPriceZWL;
    private Double newPriceZWL;

    @Enumerated(EnumType.STRING)
    private AdjustmentType reason;

    private LocalDateTime adjustmentDate;

    public enum AdjustmentType {
        MANUAL,
        BROKERAGE,
        EXPIRY,
        PROMOTION,
        SUPPLIER_CHANGE
    }

    @PrePersist
    public void onCreate() {
        this.adjustmentDate = LocalDateTime.now();
    }
}
