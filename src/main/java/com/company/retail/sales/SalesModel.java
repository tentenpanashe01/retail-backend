package com.company.retail.sales;

import com.company.retail.shop.ShopModel;
import com.company.retail.user.UserModel;
import com.company.retail.saleItem.SaleItemModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SalesModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long saleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ShopModel shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UserModel cashier;

    private LocalDateTime saleDate;

    private Double totalAmountUSD;
    private Double totalAmountZWL;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("sale")
    private List<SaleItemModel> saleItems = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        this.saleDate = LocalDateTime.now();
    }

    public void addItem(SaleItemModel item) {
        saleItems.add(item);
        item.setSale(this);
    }

    public enum PaymentMethod {
        CASH, CARD, ECOCASH, SWIPE
    }
}