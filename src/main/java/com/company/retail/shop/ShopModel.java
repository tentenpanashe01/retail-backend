package com.company.retail.shop;

import com.company.retail.product.ProductModel;
import com.company.retail.stock.StockModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "shops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ShopModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shopName;
    private String location;
    private String contactNumber;
    private String managerName;
    private LocalDateTime dateCreated;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<com.company.retail.ShopStock.ShopStockModel> shopStocks;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StockModel> stocks;


    @PrePersist
    public void onCreate() {
        this.dateCreated = LocalDateTime.now();
    }

}
