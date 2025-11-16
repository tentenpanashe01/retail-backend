package com.company.retail.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false, unique = true)
    private String productName;

    private String category;
    private String unit;
    private Integer reorderLevel;

    // 💰 Selling prices are global (apply to all shops)
    private Double sellingPriceUSD;
    private Double sellingPriceZWL;

    @Column(updatable = false)
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;

    @PrePersist
    public void onCreate() {
        dateCreated = LocalDateTime.now();
        dateUpdated = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        dateUpdated = LocalDateTime.now();
    }


// ✅ Lightweight constructor for ID-only references
    public ProductModel(Long productId) {
        this.productId = productId;
    }
}
