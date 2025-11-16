package com.company.retail.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository extends JpaRepository<ShopModel, Long> {
    boolean existsByShopNameIgnoreCase(String shopName);
}